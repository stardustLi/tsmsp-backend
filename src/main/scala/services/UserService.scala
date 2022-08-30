package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._

import models.fields.{IDCard, UserName}
import models.{User, UserPermission, UserToken}
import tables.{UserPermissionTableInstance, UserTable, UserTableInstance, UserTokenTable, UserTokenTableInstance}
import utils.db.await
import utils.string.randomToken

object UserService {
  val LOGGER: Logger = Logger("UserService")

  def login(userName: UserName, password: String, now: DateTime): Try[String] = Try {
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

  def register(userName: UserName, password: String, realName: String, idCard: IDCard, now: DateTime): Try[String] = Try {
    val userQuery: Query[UserTable, User, Seq] = UserTableInstance.filterByUserName(userName)
    await(
      userQuery.result.flatMap(
        user => {
          if (user.nonEmpty) throw exceptions.UserNameAlreadyExists()
          val token = randomToken(length = 30)
          var op: DBIO[Int] =
            (UserTableInstance.instance += User(userName, password, realName, idCard)) >>
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
        checkPermission(token, now).map(
          {
            case None => throw exceptions.NoPermission()
            case Some(permission) =>
              if (!permission.admin) throw exceptions.NoPermission()
          }
        ) >>
          UserPermissionTableInstance.instance.insertOrUpdate(permission)
        )
        .transactionally
    )
  }

  def getProfile(token: String, now: DateTime): Try[User] = Try {
    await (
      findUserByToken(token, now).flatMap(userName => {
        UserTableInstance.filterByUserName(userName)
          .result
          .head
      }).transactionally
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
  def findUserByIDCard(idCard: IDCard): DBIO[UserName] =
    UserTableInstance.instance
      .filter(
        user => user.idCard === idCard
      )
      .result
      .map(
        user => {
          if (user.isEmpty) throw exceptions.IDCardNotExists(idCard)
          user.head.userName
        }
      )
  def checkUserHasAccess(user: UserName, other: UserName): DBIO[Boolean] =
    // TODO: to finish
    DBIO.successful(user == other)
  def checkUserHasAccessByTokenAndIDCard(token: String, idCard: IDCard, now: DateTime): DBIO[Boolean] =
    (findUserByToken(token, now) zip findUserByIDCard(idCard))
      .flatMap(result => checkUserHasAccess(result._1, result._2))

  def getUserPermission(userName: UserName): DBIO[Option[UserPermission]] =
    UserPermissionTableInstance.filterByUserName(userName).result.headOption

  def checkPermission(token: String, now: DateTime): DBIO[Option[UserPermission]] =
    findUserByToken(token, now).flatMap(getUserPermission)

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
