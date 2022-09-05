package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import slick.jdbc.PostgresProfile.api._
import models.fields.{IDCard, Password, UserName}
import models.{User, UserAdminPermission, UserOthersQuery, UserToken}
import tables.{UserAdminPermissionTableInstance, UserOthersQueryTableInstance, UserTable, UserTableInstance, UserTokenTable, UserTokenTableInstance}
import utils.db.await
import utils.string.randomToken

object UserService {
  val LOGGER: Logger = Logger("UserService")

  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
  /**
   * 登录
   * @param userName 用户名
   * @param password 密码
   * @return token
   */
  def login(userName: UserName, password: Password, now: DateTime): Try[String] = Try {
    val userQuery: Query[UserTable, User, Seq] = UserTableInstance.filterByUserPass(userName, password)
    await(
      userQuery.result.flatMap(
        user => {
          if (user.isEmpty) throw exceptions.WrongPassword()
          checkToken(userName, now)
        }
      ).transactionally
    )
  }

  /**
   * 注册
   * @param userName 用户名
   * @param password 密码
   * @param realName 真实姓名
   * @param idCard 身份证号
   * @return token
   */
  def register(userName: UserName, password: Password, realName: String, idCard: IDCard, now: DateTime): Try[String] = Try {
    val userQuery: Query[UserTable, User, Seq] = UserTableInstance.filterByUserName(userName)
    if (!userName.isValid()) throw exceptions.UserNameInvalid(userName)
    if (!password.isValid()) throw exceptions.PasswordInvalid()
    if (!idCard.isValid()) throw exceptions.IDCardInvalid(idCard)
    await(
      userQuery.result.flatMap(
        user => {
          if (user.nonEmpty) throw exceptions.UserNameAlreadyExists()
          val token = randomToken(length = 30)
          var op: DBIO[Int] =
            (UserTableInstance.instance += User(userName, password, realName, idCard.toLowerCase())) >>
            (UserTokenTableInstance.instance += UserToken(userName, token, now.getMillis))
          if (userName.value == "root") {
            op = op >>
              (UserAdminPermissionTableInstance.instance += UserAdminPermissionTableInstance.all(userName))
          }
          op >> DBIO.successful(token)
        }
      ).transactionally
    )
  }

  /**
   * 更改密码
   * @param newPassword 新密码
   * @return 新 token
   */
  def changePassword(token: String, newPassword: Password, now: DateTime): Try[String] = Try {
    await(
      apiFindUserByToken(token, now).flatMap(
        userName => {
          if (!newPassword.isValid()) throw exceptions.PasswordInvalid()
          (
            UserTableInstance.filterByUserName(userName)
              .map(user => user.password)
              .update(newPassword)
          ) >> (
            checkToken(userName, now, forceUpdate = true)
          )
        }
      ).transactionally
    )
  }

  /**
   * 获取用户的管理权限
   * @return 管理权限列表
   */
  def apiGetAdminPermission(token: String, now: DateTime): Try[Option[UserAdminPermission]] = Try {
    await(
      apiFindUserByToken(token, now).flatMap(
        userName =>
          UserAdminPermissionTableInstance.filterByUserName(userName)
            .result
            .headOption
      ).transactionally
    )
  }

  /**
   * 设置用户的管理权限
   * @param permission 管理权限列表
   * @return 1
   */
  def apiSetAdminPermission(token: String, permission: UserAdminPermission, now: DateTime): Try[Int] = Try {
    await(
      (
        checkAdminPermission(token, _.admin, now) >>
        UserAdminPermissionTableInstance.instance.insertOrUpdate(permission)
      ).transactionally
    )
  }

  /**
   * 获取用户信息
   * @return 用户信息列表
   */
  def apiGetProfile(token: String, now: DateTime): Try[User] = Try {
    await(
      apiFindUserByToken(token, now).flatMap(getProfile).transactionally
    )
  }

  /**
   * 授予访问权限
   * @param trusted 被授予的用户
   * @return 1
   */
  def grantPermission(token: String, trusted: UserName, now: DateTime): Try[Int] = Try {
    await(
      apiFindUserByToken(token, now).flatMap(
        userName =>
          UserTableInstance.filterByUserName(userName)
            .map(user => user.idCard)
            .result
            .head
      ).flatMap(
        idCard =>
          UserOthersQueryTableInstance.instance += UserOthersQuery(trusted, idCard.toLowerCase())
      ).transactionally
    )
  }

  /**
   * 撤回访问权限
   * @param trusted 被撤回的用户
   * @return 1
   */
  def revokePermission(token: String, trusted: UserName, now: DateTime): Try[Int] = Try {
    await(
      apiFindUserByToken(token, now).flatMap(
        userName =>
          UserTableInstance.filterByUserName(userName)
            .map(user => user.idCard)
            .result
            .head
      ).flatMap(
        idCard =>
          UserOthersQueryTableInstance.filterByUserIDCard(trusted, idCard).delete
      ).transactionally
    )
  }

  /**
   * 获取信任用户的列表
   * @return List[String]，信任用户名的列表
   */
  def fetchAllGrantedUsers(token: String, now: DateTime): Try[List[String]] = Try {
    await (
      apiFindUserByToken(token, now).flatMap(
        userName =>
          UserTableInstance.filterByUserName(userName)
            .map(user => user.idCard)
            .result
            .head
      ).flatMap(
        idCard => {
          UserOthersQueryTableInstance
            .filterByIDCard(idCard)
            .map(userOther => userOther.userName)
            .result
        }
      ).transactionally
    ).toList.map(userName => userName.value)
  }

  /**
   * 检查用户是否有能力访问身份证号为 idCard 的人
   *
   * @param token  用户 token
   * @param idCard 身份证号
   * @return 成功返回 Unit，失败抛出错误
   */
  def checkUserHasAccessByTokenAndIDCard(token: String, idCard: IDCard, now: DateTime): Try[Boolean] = Try {
    Try {
      await(
        apiCheckUserHasAccessByTokenAndIDCard(token, idCard, now).transactionally
      )
    } match {
      case Success(_) => true
      case Failure(_: exceptions.NoAccessOfIdCard) => false
      case Failure(exc) => throw exc
    }
  }

  /******** 内部 API ********/
  /**
   * 根据 token 返回用户
   * @param token 用户 token
   * @return 对应用户
   */
  def apiFindUserByToken(token: String, now: DateTime): DBIO[UserName] =
    UserTokenTableInstance.instance
      .filter(
        user => user.token === token && user.refreshTime >= now.minusHours(2).getMillis
      )
      .result
      .map(
        user => {
          if (user.isEmpty) throw exceptions.TokenNotExists()
          user.head.userName
        }
      )
  /**
   * 检查用户是否有能力访问身份证号为 idCard 的人
   * @param token 用户 token
   * @param idCard 身份证号
   * @return 成功返回 Unit，失败抛出错误
   */
  def apiCheckUserHasAccessByTokenAndIDCard(token: String, idCard: IDCard, now: DateTime): DBIO[Unit] =
    apiFindUserByToken(token, now)
      .flatMap(
        userName =>
          UserTableInstance.filterByUserIDCard(userName, idCard).exists.result zip
          UserOthersQueryTableInstance.filterByUserIDCard(userName, idCard).exists.result
      ).map(x => {
        if (!(x._1 || x._2)) throw exceptions.NoAccessOfIdCard(idCard)
      })

  /**
   * 根据用户名获取用户信息
   * @param userName 用户名
   * @return 对应用户信息
   */
  def getProfile(userName: UserName): DBIO[User] =
    UserTableInstance.filterByUserName(userName).result.head

  /**
   * 检查用户是否拥有某种管理权限
   * @param token 用户 token
   * @param predicate 根据 UserAdminPermission 返回是否有权限的函数的谓词
   * @return 成功返回 Unit，失败抛出错误
   */
  def checkAdminPermission(token: String, predicate: UserAdminPermission => Boolean, now: DateTime): DBIO[Unit] =
    apiFindUserByToken(token, now).flatMap(
      userName =>
        UserAdminPermissionTableInstance.filterByUserName(userName).result.headOption
    ).map(
      {
        case Some(permission) if predicate(permission) => Unit
        case _ => throw exceptions.NoPermission()
      }
    )

  /**
   * 检查用户 token 是否过期，并返回新 token
   * @param userName 用户名
   * @return 当前有效 token
   */
  def checkToken(userName: UserName, now: DateTime, forceUpdate: Boolean = false): DBIO[String] = {
    val tokenQuery: Query[UserTokenTable, UserToken, Seq] = UserTokenTableInstance.filterByUserName(userName)
    tokenQuery.result.flatMap(
      user => {
        if (user.isEmpty) throw exceptions.UserNotExists()
        val entry: UserToken = user.head

        if (!forceUpdate && entry.refreshTime >= now.minusHours(2).getMillis) {
          UserTokenTableInstance.filterByUserName(userName).map(
            user => user.refreshTime
          ).update(
            now.getMillis
          ).andThen(
            DBIO.successful(entry.token)
          )
        } else {
          val newToken = randomToken(30)
          UserTokenTableInstance.filterByUserName(userName).map(
            user => (user.token, user.refreshTime)
          ).update(
            (newToken, now.getMillis)
          ).andThen(
            DBIO.successful(newToken)
          )
        }
      }
    )
  }
}
