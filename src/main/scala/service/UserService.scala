package service

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
    val userQuery: Query[UserTable, User, Seq] = UserTableInstance.filterByUserPass(userName, password).get
    await(
      userQuery.result.flatMap(
        user => {
          if (user.isEmpty) throw exceptions.WrongPassword()
          futureCheckToken(userName, now).get
        }
      ).transactionally
    )
  }

  def register(userName: UserName, password: String, realName: String, idCard: IDCard, now: DateTime): Try[String] = Try {
    val userQuery: Query[UserTable, User, Seq] = UserTableInstance.filterByUserName(userName).get
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

  def findUserByToken(token: String, now: DateTime): Try[DBIO[UserName]] = Try {
    UserTokenTableInstance.instance
      .filter(
        user => user.token === token && user.refreshTime >= now.minusHours(2).getMillis()
      )
      .result
      .map(
        user => {
          if (user.isEmpty) throw exceptions.TokenNotExists()
          user.head.userName
        }
      )
  }

  def findUserByIDCard(idCard: IDCard, now: DateTime): Try[DBIO[UserName]] = Try {
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
  }

  def checkUserHasAccess(user: UserName, other: UserName): Try[DBIO[Boolean]] = Try {
    // TODO: to finish
    DBIO.successful(user == other)
  }

  def checkUserHasAccessByTokenAndIDCard(token: String, idCard: IDCard, now: DateTime): Try[DBIO[Boolean]] = Try {
    (findUserByToken(token, now).get zip findUserByIDCard(idCard, now).get)
      .flatMap(result => checkUserHasAccess(result._1, result._2).get)
  }

  def getUserPermission(userName: UserName): Try[DBIO[Option[UserPermission]]] = Try {
    UserPermissionTableInstance.filterByUserName(userName).get.result.headOption
  }

  def apiSetPermission(token: String, permission: UserPermission, now: DateTime) = Try {
    await(
      (
        findUserByToken(token, now).get.flatMap(userName => getUserPermission(userName).get).map(
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

  def futureCheckToken(userName: UserName, now: DateTime): Try[DBIO[String]] = Try {
    val tokenQuery: Query[UserTokenTable, UserToken, Seq] = UserTokenTableInstance.filterByUserName(userName).get
    tokenQuery.result.flatMap(
      user => {
        if (user.isEmpty) throw exceptions.UserNotExists()
        val entry: UserToken = user.head

        LOGGER.info(entry.userName + ", " + entry.token + ", " + entry.refreshTime)

        if (entry.refreshTime >= now.minusHours(2).getMillis) {
          UserTokenTableInstance.filterByUserName(userName).get.map(
            user => user.refreshTime
          ).update(
            now.getMillis
          ).andThen(
            DBIO.successful(entry.token)
          )
        } else {
          val newToken = randomToken(30)
          UserTokenTableInstance.filterByUserName(userName).get.map(
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
