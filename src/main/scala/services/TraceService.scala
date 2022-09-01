package services

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import models.fields.{IDCard, UserName}
import models.{Trace, UserTrace, UserTraceWithPeople}
import tables.{UserTraceTable, UserTraceTableInstance, UserTraceWithPeopleTableInstance}
import utils.db.await


object TraceService {
  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
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

  def apiGetTraces(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[UserTrace]] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        getTraces(idCard, startTime, endTime)
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
  def apiGetTracesWithPeople(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[PartialTrace]] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        getTracesWithPeople(idCard, startTime, endTime)
      }).transactionally
    ).map(trace => PartialTrace(trace.ThisPeople, trace.CCUserName, trace.time)).toList
  }

  def apiGetTracesWithPeopleWithIDCard(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[UserTraceWithPeople]] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.readTraceId, now) >>
        getTracesWithPeople(idCard, startTime, endTime)
      ).transactionally
    ).toList
  }

  /******** 内部 API ********/
  /**
   * 查询身份证号为 idCard 的用户在 [startTime, endTime] 之间经过的轨迹列表
   * @param idCard 身份证号
   * @param startTime 起始时间
   * @param endTime 终止时间
   * @return
   */
  def getTraces(idCard: IDCard, startTime: Long, endTime: Long): DBIO[Seq[UserTrace]] =
    UserTraceTableInstance
      .filterByIDCard(idCard)
      .filter(trace => trace.time.between(startTime, endTime))
      .sortBy(trace => trace.time)
      .result

  /**
   * 查询身份证号为 idCard 的用户在 [startTime, endTime] 之间的密接列表
   *
   * @param idCard    身份证号
   * @param startTime 起始时间
   * @param endTime   终止时间
   * @return
   */
  def getTracesWithPeople(idCard: IDCard, startTime: Long, endTime: Long): DBIO[Seq[UserTraceWithPeople]] =
    UserTraceWithPeopleTableInstance
      .filterByIDCard(idCard)
      .filter(trace => trace.time.between(startTime, endTime))
      .sortBy(trace => trace.time)
      .result

}
