package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._

import models.fields.{IDCard, UserName}
import models.{Trace, UserTrace, UserTraceWithPeople}
import tables.{UserTraceTableInstance, UserTraceWithPeopleTableInstance}
import utils.db.await

object TraceService {
  val LOGGER: Logger = Logger("TraceService")

  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
  /** 添加地点轨迹 */
  def addTrace(userToken: String, idCard: IDCard, trace: Trace, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        (
          UserTraceTableInstance.instance += UserTrace(idCard.toLowerCase(), trace, now.getMillis)
        )
      ).transactionally
    )
  }

  /** 删除地点轨迹 */
  def removeTrace(userToken: String, idCard: IDCard, time: Long, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        UserTraceTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time === time)
          .delete
      ).transactionally
    )
  }

  /** 更新地点轨迹 */
  def updateTrace(userToken: String, idCard: IDCard, time: Long, trace: Trace, now: DateTime): Try[Int] = Try {
    import models.types.CustomColumnTypes._
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        UserTraceTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time === time)
          .map(trace => trace.trace)
          .update(trace)
      ).transactionally
    )
  }

  /** 获取所有地点轨迹 */
  def apiGetTraces(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[UserTrace]] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        getTraces(idCard, startTime, endTime)
      ).transactionally
    ).toList
  }

  /** 添加密接轨迹 */
  def addTraceWithPeople(userToken: String, idCard: IDCard, cc: UserName, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        UserService.getProfile(cc)
      ).flatMap(
        ccUser =>
          UserTraceWithPeopleTableInstance.instance += UserTraceWithPeople(
            idCard.toLowerCase(), ccUser.userName, ccUser.idCard, now.getMillis
          )
      ).transactionally
    )
  }

  /** 删除密接轨迹 */
  def removeTraceWithPeople(userToken: String, idCard: IDCard, time: Long, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        UserTraceWithPeopleTableInstance
          .filterByIDCard(idCard)
          .filter(trace => trace.time === time)
          .delete
      ).transactionally
    )
  }

  /** 更新密接轨迹 */
  def updateTraceWithPeople(userToken: String, idCard: IDCard, time: Long, cc: UserName, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        UserService.getProfile(cc)
      ).flatMap(
        ccUser =>
          UserTraceWithPeopleTableInstance
            .filterByIDCard(idCard)
            .filter(trace => trace.time === time)
            .map(trace => (trace.CCUserName, trace.CCIDCard))
            .update((ccUser.userName, ccUser.idCard))
      ).transactionally
    )
  }

  /** 获取所有密接轨迹，去除身份证号敏感信息 */
  case class PartialTrace(ThisPeople: IDCard, CCUserName: UserName, time: Long)
  def apiGetTracesWithPeople(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[PartialTrace]] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        getTracesWithPeople(idCard, startTime, endTime)
      ).transactionally
    ).map(trace => PartialTrace(trace.ThisPeople, trace.CCUserName, trace.time)).toList
  }

  /** 获取所有密接轨迹，包含身份证号敏感信息 */
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
   * @return 轨迹的 Seq
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
   * @param idCard 身份证号
   * @param startTime 起始时间
   * @param endTime 终止时间
   * @return 密接的 Seq
   */
  def getTracesWithPeople(idCard: IDCard, startTime: Long, endTime: Long): DBIO[Seq[UserTraceWithPeople]] =
    UserTraceWithPeopleTableInstance
      .filterByIDCard(idCard)
      .filter(trace => trace.time.between(startTime, endTime))
      .sortBy(trace => trace.time)
      .result
}
