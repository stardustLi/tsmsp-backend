package service

import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._

import models.fields.{IDCard, UserName}
import models.{User, UserToken}
import tables.{UserTable, UserTableInstance, UserTokenTable, UserTokenTableInstance}
import utils.db.await
import utils.string.randomToken

object UserService {
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
      userQuery.result.map(
        user => {
          if (!user.isEmpty) throw exceptions.UserNameAlreadyExists()
          val token = randomToken(30)
          (
            (UserTableInstance.instance += User(userName, password, realName, idCard)) >>
            (UserTokenTableInstance.instance += UserToken(userName, token, now.getMillis()))
          )
          token
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

  def futureCheckToken(userName: UserName, now: DateTime): Try[DBIO[String]] = Try {
    val tokenQuery: Query[UserTokenTable, UserToken, Seq] = UserTokenTableInstance.filterByUserName(userName).get
    tokenQuery.result.map(
      user => {
        if (user.isEmpty) throw exceptions.UserNotExists()
        val entry: UserToken = user.head
        if (entry.refreshTime >= now.minusHours(2).getMillis()) {
          UserTokenTableInstance.filterByUserName(userName).get.map(
            user => user.refreshTime
          ).update(
            now.getMillis()
          )

          entry.token
        } else {
          val newToken = randomToken(30)
          UserTokenTableInstance.filterByUserName(userName).get.map(
            user => (user.token, user.refreshTime)
          ).update(
            (newToken, now.getMillis())
          )

          newToken
        }
      }
    )
  }
}
