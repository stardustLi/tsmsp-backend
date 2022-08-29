package service

import models.{Appeal, Trace}
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import tables.{UserAppealTable, UserAppealTableInstance, UserTable, UserTraceTableInstance}
import utils.db.await
import utils.string.randomToken

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object CodeService {

  def addAppeal(idCard: String, realName: String, reason: String,time: DateTime): Try[Int] = Try {
    val userQuery: Query[UserAppealTable, Appeal, Seq] = UserAppealTableInstance.filterByIdCard(idCard).get
    await(
      userQuery.result.flatMap(
        user => {
          if (user.nonEmpty) throw exceptions.UserNameAlreadyExists()
          //val token = randomToken(30)
            (UserAppealTableInstance.instance += Appeal(realName, idCard, reason))
        }
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
}
//  def updateTrace(userToken: String, time: Long, trace: Trace, now: DateTime) = Try {
//
//  }
//
//  def getTraces(userToken: String, startTime: Long, endTime: Long, now: DateTime) = Try {
//
//  }
