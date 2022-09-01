package services

import models.enums.CodeColor
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import models.{Appeal, JingReport}
import models.fields.IDCard
import tables.{JingReportTableInstance, UserAppealTableInstance, UserColorTableInstance}
import utils.db.await

object CodeService {
  def addAppeal(userToken: String, idCard: IDCard, reason: String, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserAppealTableInstance.instance += Appeal(idCard.toLowerCase(), reason, now.getMillis)
      }).transactionally
    )
  }

  def jingReport(userToken: String, idCard: IDCard, reason: String, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        JingReportTableInstance.instance += JingReport(idCard.toLowerCase(), reason, now.getMillis)
      }).transactionally
    )
  }

  def queryAppeal(userToken: String, idCard: IDCard, now: DateTime): Try[Option[Appeal]] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.viewAppeals, now) >>
        UserAppealTableInstance.filterByIdCard(idCard).result.headOption
      ).transactionally
    )
  }

  def queryAppeals(userToken: String, now: DateTime): Try[List[Appeal]] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.viewAppeals, now) >>
        UserAppealTableInstance.instance.result
      ).transactionally
    ).toList
  }

  def resolveAppeal(userToken: String, idCard: IDCard, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.viewAppeals, now) >>
        UserAppealTableInstance.filterByIdCard(idCard).delete
      ).transactionally
    )
  }

  def getColor(userToken: String, idCard: IDCard, now: DateTime): Try[CodeColor] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(
        hasAccess => {
          if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
          getColorOfTraces(idCard, now) zip
          getColorOfTraceWithPeoples(idCard, now)
        }
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
          (
            UserColorTableInstance
            .filterByIDCard(idCard)
            .map(userColor => userColor.color)
            .update(color)
          ) >> (
            DBIO.successful(color)
          )
      ).transactionally
    )
  }

//轨迹是否与中高风险区重合
  //密接者中是否有红黄码
  //红1黄2弹窗3绿4

  def getColorOfTraces(idCard: IDCard, now: DateTime): DBIO[CodeColor] =
    TraceService.getTraces(idCard, now.minusDays(14).getMillis, now.getMillis)
      .flatMap(
        userTraces => {
          val traces = userTraces.map(trace => trace.trace).toList
          DangerousPlaceService.getMostRiskLevel(traces)
        }
      ).map(
        {
          case Some(riskLevel) => riskLevel.color
          case None => CodeColor.GREEN
        }
      )

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
   *
   * @param people 身份证号列表
   * @return
   */
  def getMostDangerousColor(people: List[IDCard]): DBIO[Option[CodeColor]] = {
    UserColorTableInstance.instance.filter(
      userColor => userColor.idCard.inSet(people)
    ).map(
      userColor => userColor.color
    ).max.result
  }

}
