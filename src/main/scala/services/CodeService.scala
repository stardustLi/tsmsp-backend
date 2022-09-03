package services

import com.typesafe.scalalogging.Logger
import models.enums.{CodeColor, RiskLevel}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import models.{Appeal, DangerousPlace, JingReport, Trace}
import models.fields.IDCard
import tables.{DangerousPlaceTableInstance, JingReportTableInstance, UserAppealTableInstance, UserColorTableInstance}
import utils.db.await

object CodeService {
  val LOGGER: Logger = Logger("CodeService")

  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
  /** 添加申诉 */
  def addAppeal(userToken: String, idCard: IDCard, reason: String, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        (
          UserAppealTableInstance.instance += Appeal(idCard.toLowerCase(), reason, now.getMillis)
        )
      ).transactionally
    )
  }

  /** 添加报备 */
  def jingReport(userToken: String, idCard: IDCard, reason: String, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        (
          JingReportTableInstance.instance += JingReport(idCard.toLowerCase(), reason, now.getMillis)
        )
      ).transactionally
    )
  }

  /** 获取一个人的申诉 */
  def queryAppeal(userToken: String, idCard: IDCard, now: DateTime): Try[Option[Appeal]] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.viewAppeals, now) >>
        UserAppealTableInstance.filterByIdCard(idCard).result.headOption
      ).transactionally
    )
  }

  /** 获取所有申诉 */
  def queryAppeals(userToken: String, now: DateTime): Try[List[Appeal]] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.viewAppeals, now) >>
        UserAppealTableInstance.instance.result
      ).transactionally
    ).toList
  }

  /** 解决 (移除) 一个申诉 */
  def resolveAppeal(userToken: String, idCard: IDCard, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.viewAppeals, now) >>
        UserAppealTableInstance.filterByIdCard(idCard).delete
      ).transactionally
    )
  }

  /** 获取一个地区的风险程度 */
  def dangerousQuery(place: Trace): Try[RiskLevel] = Try {
    await(
      (
        DangerousPlaceTableInstance.filterByPlace(place)
          .map(place => place.level)
          .result
          .head
        ).transactionally
    )
  }

  /** 更新一个地区的风险程度 */
  def dangerousUpdate(userToken: String, place: Trace, level: RiskLevel, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.setRiskAreas, now) >>
          DangerousPlaceTableInstance.instance.insertOrUpdate(DangerousPlace(place, level))
        ).transactionally
    )
  }

  /** 获取用户健康码颜色 */
  def getColor(userToken: String, idCard: IDCard, now: DateTime): Try[CodeColor] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        (
          getColorOfTraces(idCard, now) zip
          getColorOfTraceWithPeoples(idCard, now)
        )
      ).map(
        colors => CodeColor.max(colors._1, colors._2)
      ).zip(
        UserColorTableInstance
          .filterByIDCard(idCard)
          .map(userColor => userColor.color)
          .result.head
      ).map(
        colors => CodeColor.max(colors._1, colors._2)
      ).flatMap(
        color =>
          updateColor(idCard, color) >>
          DBIO.successful(color)
      ).transactionally
    )
  }

  /**
   * 管理员手动更新健康码颜色
   * @param idCard 身份证号
   * @param color 健康码颜色
   * @return 1
   */
  def adminSetColor(userToken: String, idCard: IDCard, color: CodeColor, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.assignColor, now) >>
        updateColor(idCard, color)
      ).transactionally
    )
  }

  /******** 内部 API ********/
  /**
   * 根据地点轨迹确定健康码颜色
   * @param idCard 身份证号
   * @param now 当前时间 (用于计算 14 天）
   * @return 健康码颜色
   */
  def getColorOfTraces(idCard: IDCard, now: DateTime): DBIO[CodeColor] =
    TraceService.getTraces(idCard, now.minusDays(14).getMillis, now.getMillis)
      .flatMap(
        userTraces => {
          val traces = userTraces.map(trace => trace.trace).toList
          getMostRiskLevel(traces)
        }
      ).map(
        {
          case Some(riskLevel) => riskLevel.color
          case None => CodeColor.GREEN
        }
      )

  /**
   * 根据密接轨迹确定健康码颜色
   * @param idCard 身份证号
   * @param now    当前时间 (用于计算 14 天）
   * @return 健康码颜色
   * @return
   */
  def getColorOfTraceWithPeoples(idCard: IDCard, now: DateTime): DBIO[CodeColor] =
    TraceService.getTracesWithPeople(idCard, now.minusDays(14).getMillis, now.getMillis)
      .flatMap(
        userTracesWithPeople => {
          val people = userTracesWithPeople.map(trace => trace.CCIDCard).toList
          getMostDangerousColor(people)
        }
      ).map(color => color.getOrElse(CodeColor.GREEN))

  /**
   * 获取 people 中最危险的码的颜色
   * @param people 身份证号列表
   * @return
   */
  def getMostDangerousColor(people: List[IDCard]): DBIO[Option[CodeColor]] =
    UserColorTableInstance.instance.filter(
      userColor => userColor.idCard.inSet(people)
    ).map(
      userColor => userColor.color
    ).max.result

  /**
   * 获取 places 中风险度最高地区的风险度
   * @param places 地区列表
   * @return 最高的风险度
   */
  def getMostRiskLevel(places: List[Trace]): DBIO[Option[RiskLevel]] = {
    import models.types.CustomColumnTypes._
    DangerousPlaceTableInstance.instance.filter(
      dangerousPlace => dangerousPlace.place.inSet(places)
    ).map(
      dangerousPlace => dangerousPlace.level
    ).max.result
  }

  /**
   * 更新用户健康码颜色
   * @param idCard 身份证号
   * @param color 健康码颜色
   * @return 1
   */
  def updateColor(idCard: IDCard, color: CodeColor): DBIO[Int] =
    UserColorTableInstance
      .filterByIDCard(idCard)
      .map(userColor => userColor.color)
      .update(color)
}
