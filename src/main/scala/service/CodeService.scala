package service

import models.Trace
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import tables.{UserTable, UserTraceTableInstance}
import utils.db.await

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object CodeService {

  def addAppeal(idCard: String, realName: String, reason: String): Try[String] = Try {
    val userQuery: Query[UserTable, User, Seq] = UserTableInstance.filterByUserName(userName).get
    await(
      userQuery.result.flatMap(
        user => {
          if (user.nonEmpty) throw exceptions.UserNameAlreadyExists()
          val token = randomToken(30)
          (
            (UserTableInstance.instance += User(userName, password, realName, idCard)) >>
              (UserTokenTableInstance.instance += UserToken(userName, token, now.getMillis))
            ).zip(DBIO.successful(token))
        }
      ).map(
        result => result._2
      ).transactionally
    )
  }

  def removeTrace(userToken: String, time: Long, now: DateTime): Try[Int] = Try {
    await(
      UserService.findUserByToken(userToken, now).get.flatMap(userName => {
        UserTraceTableInstance
          .filterByUserName(userName).get
          .filter(trace => trace.time === time)
          .delete
      }).transactionally
    )
  }

  def updateTrace(userToken: String, time: Long, trace: Trace, now: DateTime) = Try {
    
  }

  def getTraces(userToken: String, startTime: Long, endTime: Long, now: DateTime) = Try {
    
  }
