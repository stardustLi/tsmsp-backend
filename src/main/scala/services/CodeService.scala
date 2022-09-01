package services

import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._

import models.{Appeal, JingReport}
import models.fields.IDCard
import tables.{JingReportTableInstance, UserAppealTableInstance}
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
}
