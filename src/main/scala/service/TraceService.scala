package service

import models.Trace
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import tables.UserTraceTableInstance
import utils.db.await

object TraceService {
  def addTrace(userToken: String, trace: Trace, now: DateTime) = Try {

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
  /*

  def checkTrace(userName: String, startTime: Long, endTime: Long): Try[List[UserTraceRow]] = Try {
    await(
      userTraceTable
        .filter(
          trace => trace.userName === userName && trace.time.between(startTime, endTime)
        )
        .sortBy(trace => trace.time)
        .result
    ).toList
  }

  def removeTrace(userName: String, time: Long): Try[DBIO[Int]] = Try {
  }

  def updateTrace(userName: String, trace: String, time: Long): Try[DBIO[Int]] = Try {
    userTraceTable
      .filter(
        trace => trace.userName === userName && trace.time === time
      )
      .map(trace => trace.trace)
      .update(trace)
  }
   */
}
