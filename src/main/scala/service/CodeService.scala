package service

import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._

import models.Appeal
import models.fields.IDCard
import tables.UserAppealTableInstance
import utils.db.await

object CodeService {
  def addAppeal(userToken: String, idCard: IDCard, reason: String, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserAppealTableInstance.instance += Appeal(idCard, reason, now.getMillis)
      }).transactionally
    )
  }

  def queryAppeal(userToken: String, idCard: IDCard, now: DateTime): Try[Option[Appeal]] = Try {
    await(
      (
        UserService.checkPermission(userToken, now).map(
          {
            case None => throw exceptions.NoPermission()
            case Some(permission) =>
              if (!permission.viewAppeals) throw exceptions.NoPermission()
          }
        ) >>
          UserAppealTableInstance.filterByIdCard(idCard).result.headOption
        ).transactionally
    )
  }

  def queryAppeals(userToken: String, now: DateTime): Try[List[Appeal]] = Try {
    await(
      (
        UserService.checkPermission(userToken, now).map(
          {
            case None => throw exceptions.NoPermission()
            case Some(permission) =>
              if (!permission.viewAppeals) throw exceptions.NoPermission()
          }
        ) >>
          UserAppealTableInstance.instance.result
      ).transactionally
    ).toList
  }

  def resolveAppeal(userToken: String, idCard: IDCard, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkPermission(userToken, now).map(
          {
            case None => throw exceptions.NoPermission()
            case Some(permission) =>
              if (!permission.viewAppeals) throw exceptions.NoPermission()
          }
        ) >>
          UserAppealTableInstance.filterByIdCard(idCard).delete
        ).transactionally
    )
  }
}
