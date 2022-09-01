package services

import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._

import models.fields.{IDCard, UserName}
import models.{Trace, UserTrace, UserTraceWithPeople}
import tables.{UserTraceTableInstance, UserTraceWithPeopleTableInstance}
import utils.db.await


object TraceService {
  def addTrace(userToken: String, idCard: IDCard, trace: Trace, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserTraceTableInstance.instance += UserTrace(idCard.toLowerCase(), trace, now.getMillis)
      }).transactionally
    )
  }

  def removeTrace(userToken: String, idCard: IDCard, time: Long, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserTraceTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time === time)
          .delete
      }).transactionally
    )
  }

  def updateTrace(userToken: String, idCard: IDCard, time: Long, trace: Trace, now: DateTime): Try[Int] = Try {
    import models.types.CustomColumnTypes._
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserTraceTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time === time)
          .map(trace => trace.trace)
          .update(trace)
      }).transactionally
    )
  }

  def getTraces(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[UserTrace]] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserTraceTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time.between(startTime, endTime))
          .sortBy(trace => trace.time)
          .result
      }).transactionally
    ).toList
  }

  def addTraceWithPeople(userToken: String, idCard: IDCard, cc: UserName, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserService.getProfile(cc)
      }).flatMap(ccUser => {
        UserTraceWithPeopleTableInstance.instance += UserTraceWithPeople(
          idCard.toLowerCase(), ccUser.userName, ccUser.idCard, now.getMillis
        )
      }).transactionally
    )
  }

  def removeTraceWithPeople(userToken: String, idCard: IDCard, time: Long, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserTraceWithPeopleTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time === time)
          .delete
      }).transactionally
    )
  }

  def updateTraceWithPeople(userToken: String, idCard: IDCard, time: Long, cc: UserName, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserService.getProfile(cc)
      }).flatMap(ccUser => {
        UserTraceWithPeopleTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time === time)
          .map(trace => (trace.CCUserName, trace.CCIDCard))
          .update((ccUser.userName, ccUser.idCard))
      }).transactionally
    )
  }

  case class PartialTrace(ThisPeople: IDCard, CCUserName: UserName, time: Long)

  def getTracesWithPeople(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[PartialTrace]] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserTraceWithPeopleTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time.between(startTime, endTime))
          .sortBy(trace => trace.time)
          .result
      }).transactionally
    ).map(trace => PartialTrace(trace.ThisPeople, trace.CCUserName, trace.time)).toList
  }

  def getTracesWithPeopleWithIDCard(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[UserTraceWithPeople]] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.readTraceId, now) >>
        UserTraceWithPeopleTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time.between(startTime, endTime))
          .sortBy(trace => trace.time)
          .result
      ).transactionally
    ).toList
  }
}
