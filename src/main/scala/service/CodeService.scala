package service

import models.{Appeal, Trace}
import models.fields.IDCard
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import tables.{UserAppealTable, UserAppealTableInstance, UserTable, UserTraceTableInstance}
import utils.db.await
import utils.string.randomToken

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object CodeService {
  def addAppeal(userToken: String, idCard: IDCard, reason: String, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).get.flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.noAccessOfIdCard(idCard)
        UserAppealTableInstance.instance += Appeal(idCard, reason)
      }).transactionally
    )
  }
}
