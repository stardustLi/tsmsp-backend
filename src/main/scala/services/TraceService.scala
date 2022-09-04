package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.Breaks
import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import models.enums.TraceLevel
import models.fields.{IDCard, TraceID, UserName}
import models.{Trace, TraceTree, UserTrace, UserTraceWithPeople}
import tables.{TraceTreeTableInstance, UserTraceTableInstance, UserTraceWithPeopleTableInstance}
import utils.db.await

object TraceService {
  val LOGGER: Logger = Logger("TraceService")

  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
  /** 添加地点轨迹 */
  def addTrace(userToken: String, idCard: IDCard, trace: TraceID, now: DateTime): Try[Int] = Try {
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
  def updateTrace(userToken: String, idCard: IDCard, time: Long, trace: TraceID, now: DateTime): Try[Int] = Try {
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
  case class UserTraceDetailed(idCard: IDCard, trace: Trace, time: Long)
  def apiGetTraces(userToken: String, idCard: IDCard, startTime: Long, endTime: Long, now: DateTime): Try[List[UserTraceDetailed]] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        getTraces(idCard, startTime, endTime)
      ).transactionally
    ).flatMap(userTrace => {
      traceID2Trace(userTrace.trace) match {
        case Some(trace) => List(UserTraceDetailed(userTrace.idCard, trace, userTrace.time))
        case _ => Nil
      }
    }).toList
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

  /** 创建新地点 */
  def createPlace(userToken: String, traceDescriptor: List[String], now: DateTime): Try[Trace] = Try {
    await(UserService.checkAdminPermission(userToken, _.createPlace, now).transactionally)
    if (traceDescriptor.isEmpty) throw exceptions.PlaceIsEmpty()
    if (traceDescriptor.length > TraceLevel.objectList.length) throw exceptions.PlaceTooLong()
    LOGGER.info("%o", traceDescriptor)
    var currentLevel: TraceLevel = TraceLevel.PROVINCE
    var currentID: TraceID = TraceID(0)
    var currentTrace: Option[Trace] = None
    for (name <- traceDescriptor) {
      val nextID: TraceID = await(
        TraceTreeTableInstance
          .filterByNameLevelPID(name, currentLevel, currentID)
          .result
          .headOption
      ) match {
        case Some(place) => place.id
        case _ => await(
          TraceTreeTableInstance.instanceWithId +=
          TraceTree(TraceID(0), name, currentLevel, currentID)
        )
      }
      val trace: Trace = new Trace(nextID, name, currentLevel)
      trace.parent = currentTrace
      currentID = nextID
      currentTrace = Some(trace)
      currentLevel = currentLevel.next
    }
    currentTrace.get
  }

  /** 获取地点信息 */
  def getPlaceInfo(traceID: TraceID): Try[Option[Trace]] = Try {
    traceID2Trace(traceID)
  }

  /** 获取下属地点信息 */
  case class PartialTraceTree(id: TraceID, name: String, level: TraceLevel)
  def getPlaceSubordinates(traceID: TraceID): Try[List[PartialTraceTree]] = Try {
    await(
      (
        TraceTreeTableInstance
          .filterByPID(traceID)
          .result
      ).transactionally
    ).map(
      tree => PartialTraceTree(tree.id, tree.name, tree.level)
    ).toList
  }

  /******** 内部 API ********/
  /**
   * 将 traceID 转为 trace 对象
   * @param traceID traceID 值
   * @return 对应的 trace 对象，可能为空
   */
  def traceID2Trace(traceID: TraceID): Option[Trace] = {
    val leaf: Trace = new Trace(TraceID(0), "", TraceLevel.PROVINCE)
    var current: Trace = leaf
    var id: TraceID = traceID
    val loop: Breaks = new Breaks
    loop.breakable {
      while (true)
        await(TraceTreeTableInstance.filterByID(id).result.headOption) match {
          case Some(value) =>
            val parent: Trace = new Trace(value.id, value.name, value.level)
            current.parent = Some(parent)
            current = parent
            id = value.parentID
            if (value.level.isTop)
              loop.break
          case _ => loop.break
        }
    }
    leaf.parent
  }

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
