package service

import models.CustomColumnTypes._
import models.enums.RiskLevel
import models.{DangerousPlace, Policy, Trace}
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import tables.DangerousPlaceTableInstance
import utils.db.await

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object DangerousPlaceService {
  def dangerousQuery(place: Trace): Try[RiskLevel] = Try {
    await(
      DangerousPlaceTableInstance.filterByPlace(place).get
        .map(place => place.level)
        .result
        .head
        .transactionally
    )
  }

  def dangerousUpdate(userToken: String, place: Trace, level: RiskLevel, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.findUserByToken(userToken, now).get.flatMap(userName => UserService.getUserPermission(userName).get).map(
          {
            case None => throw exceptions.NoPermission()
            case Some(permission) =>
              if (!permission.setRiskAreas) throw exceptions.NoPermission()
          }
        ) >>
          DangerousPlaceTableInstance.instance.insertOrUpdate(DangerousPlace(place, level))
      ).transactionally
    )
  }
}
