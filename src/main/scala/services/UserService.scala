package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import models.fields.{IDCard, Password, UserName}
import models.{User, UserOthersQuery, UserPermission, UserToken}
import tables.{UserOthersQueryTableInstance, UserPermissionTableInstance, UserTable, UserTableInstance, UserTokenTable, UserTokenTableInstance}
import utils.db.await
import utils.string.randomToken

object UserService {
  val LOGGER: Logger = Logger("UserService")

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
              (UserPermissionTableInstance.instance += UserPermissionTableInstance.all(userName))
          }
          op >> DBIO.successful(token)
        }
      ).transactionally
    )
  }

  def apiSetPermission(token: String, permission: UserPermission, now: DateTime): Try[Int] = Try {
    await(
      (
        checkPermission(token, now, _.admin) >>
        UserPermissionTableInstance.instance.insertOrUpdate(permission)
      ).transactionally
    )
  }

  def getProfile(token: String, now: DateTime): Try[User] = Try {
    await(
      findUserByToken(token, now)
        .flatMap(userName => {
          UserTableInstance.filterByUserName(userName)
            .result
            .head
        }
      ).transactionally
    )
  }

  def grantPermission(token: String, trusted: UserName, now: DateTime): Try[Int] = Try {
    await(
      findUserByToken(token, now)
        .flatMap(userName => {
          UserTableInstance.filterByUserName(userName)
            .map(user => user.idCard)
            .result
            .head
        })
        .flatMap(idCard => {
          UserOthersQueryTableInstance.instance += UserOthersQuery(trusted, idCard.toLowerCase())
        }
      ).transactionally
    )
  }

  def revokePermission(token: String, trusted: UserName, now: DateTime): Try[Int] = Try {
    await(
      findUserByToken(token, now)
        .flatMap(userName => {
          UserTableInstance.filterByUserName(userName)
            .map(user => user.idCard)
            .result
            .head
        })
        .flatMap(idCard => {
          UserOthersQueryTableInstance.filterByUserIDCard(trusted, idCard).delete
        }
      ).transactionally
    )
  }

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
  def checkUserHasAccessByTokenAndIDCard(token: String, idCard: IDCard, now: DateTime): DBIO[Boolean] =
    findUserByToken(token, now)
      .flatMap(userName =>
        UserTableInstance.filterByUserIDCard(userName, idCard).exists.result zip
        UserOthersQueryTableInstance.filterByUserIDCard(userName, idCard).exists.result
      ).map(x => x._1 || x._2)

  def checkPermission(token: String, now: DateTime, predicate: UserPermission => Boolean): DBIO[Unit] =
    findUserByToken(token, now).flatMap(
      userName => UserPermissionTableInstance.filterByUserName(userName).result.headOption
    ).map(
      {
        case None => throw exceptions.NoPermission()
        case Some(permission) =>
          if (!predicate(permission)) throw exceptions.NoPermission()
      }
    )

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
