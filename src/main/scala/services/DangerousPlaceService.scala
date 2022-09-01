package services

import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import scala.util.Try

import models.types.CustomColumnTypes._
import models.enums.RiskLevel
import models.{DangerousPlace, Trace}
import tables.DangerousPlaceTableInstance
import utils.db.await

object DangerousPlaceService {
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

  def dangerousUpdate(userToken: String, place: Trace, level: RiskLevel, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkPermission(userToken, _.setRiskAreas, now) >>
        DangerousPlaceTableInstance.instance.insertOrUpdate(DangerousPlace(place, level))
      ).transactionally
    )
  }

  def DangerousPlaceQuery(level: RiskLevel): Try[List[Trace]] = Try {
    import models.types.CustomColumnTypes._
    await(
      (
        DangerousPlaceTableInstance.filterByRiskLevel(level)
          .map(place => place.place)
          .result
      ).transactionally
    ).toList
  } //用于查询当前中高风险地区
}