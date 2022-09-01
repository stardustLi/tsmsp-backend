package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import models.fields.{IDCard, Password, UserName}
import models.{User, UserOthersQuery, UserAdminPermission, UserToken}
import tables.{UserOthersQueryTableInstance, UserAdminPermissionTableInstance, UserTable, UserTableInstance, UserTokenTable, UserTokenTableInstance}
import utils.db.await
import utils.string.randomToken

object UserService {
  val LOGGER: Logger = Logger("UserService")

  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
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

  def apiSetPermission(token: String, permission: UserAdminPermission, now: DateTime): Try[Int] = Try {
    await(
      (
        checkPermission(token, _.admin, now) >>
        UserAdminPermissionTableInstance.instance.insertOrUpdate(permission)
      ).transactionally
    )
  }

  def getProfile(token: String, now: DateTime): Try[User] = Try {
    await(
      findUserByToken(token, now).flatMap(
        userName => {
          UserTableInstance.filterByUserName(userName)
            .result
            .head
        }
      ).transactionally
    )
  }

  def grantPermission(token: String, trusted: UserName, now: DateTime): Try[Int] = Try {
    await(
      findUserByToken(token, now).flatMap(
        userName => {
          UserTableInstance.filterByUserName(userName)
            .map(user => user.idCard)
            .result
            .head
        }
      ).flatMap(
        idCard => {
          UserOthersQueryTableInstance.instance += UserOthersQuery(trusted, idCard.toLowerCase())
        }
      ).transactionally
    )
  }

  def revokePermission(token: String, trusted: UserName, now: DateTime): Try[Int] = Try {
    await(
      findUserByToken(token, now).flatMap(
        userName => {
          UserTableInstance.filterByUserName(userName)
            .map(user => user.idCard)
            .result
            .head
        }
      ).flatMap(
        idCard => {
          UserOthersQueryTableInstance.filterByUserIDCard(trusted, idCard).delete
        }
      ).transactionally
    )
  }

  def fetchAllGrantedUsers(token: String, now: DateTime): Try[List[String]] = Try {
    await (
      findUserByToken(token, now).flatMap(
        userName => {
          UserTableInstance.filterByUserName(userName)
            .map(user => user.idCard)
            .result
            .head
        }
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

  /******** 内部 API ********/
  /**
   * 根据 token 返回用户
   * @param token 用户 token
   * @return 对应用户
   */
  def findUserByToken(token: String, now: DateTime): DBIO[UserName] =
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
   * @return 布尔值
   */
  def checkUserHasAccessByTokenAndIDCard(token: String, idCard: IDCard, now: DateTime): DBIO[Boolean] =
    findUserByToken(token, now)
      .flatMap(userName =>
        UserTableInstance.filterByUserIDCard(userName, idCard).exists.result zip
        UserOthersQueryTableInstance.filterByUserIDCard(userName, idCard).exists.result
      ).map(x => x._1 || x._2)

  /**
   * 检查用户是否拥有某种权限
   * @param token 用户 token
   * @param predicate 根据 UserAdminPermission 返回是否有权限的函数的谓词
   * @return
   */
  def checkPermission(token: String, predicate: UserAdminPermission => Boolean, now: DateTime): DBIO[Unit] =
    findUserByToken(token, now).flatMap(
      userName => UserAdminPermissionTableInstance.filterByUserName(userName).result.headOption
    ).map(
      {
        case None => throw exceptions.NoPermission()
        case Some(permission) =>
          if (!predicate(permission)) throw exceptions.NoPermission()
      }
    )

  /**
   * 检查用户 token 是否过期，并返回新 token
   * @param userName 用户名
   * @return
   */
  def checkToken(userName: UserName, now: DateTime): DBIO[String] = {
    val tokenQuery: Query[UserTokenTable, UserToken, Seq] = UserTokenTableInstance.filterByUserName(userName)
    tokenQuery.result.flatMap(
      user => {
        if (user.isEmpty) throw exceptions.UserNotExists()
        val entry: UserToken = user.head

        if (entry.refreshTime >= now.minusHours(2).getMillis) {
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
