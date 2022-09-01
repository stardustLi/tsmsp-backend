package services

import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._

import scala.util.Try
import models.types.CustomColumnTypes._
import models.enums.RiskLevel
import models.{DangerousPlace, Trace}
import tables.DangerousPlaceTableInstance
import utils.db.{await, db}

// TODO: merge to CodeService
object DangerousPlaceService {
  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
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
        UserService.checkAdminPermission(userToken, _.setRiskAreas, now) >>
        DangerousPlaceTableInstance.instance.insertOrUpdate(DangerousPlace(place, level))
      ).transactionally
    )
  }

  /**
   * 查询风险等级为 level 的地区列表
   * @param level 风险等级
   * @return
   */
  def dangerousPlaceQuery(level: RiskLevel): Try[List[Trace]] = Try {
    import models.types.CustomColumnTypes._
    await(
      (
        DangerousPlaceTableInstance.filterByRiskLevel(level)
          .map(place => place.place)
          .result
      ).transactionally
    ).toList
  }

  /******** 内部 API ********/
  /**
   * 获取 places 中风险度最高地区的风险度
   * @param places 地区列表
   * @return
   */
  def getMostRiskLevel(places: List[Trace]): DBIO[Option[RiskLevel]] = {
    DangerousPlaceTableInstance.instance.filter(
      dangerousPlace => dangerousPlace.place.inSet(places)
    ).map(
      dangerousPlace => dangerousPlace.level
    ).max.result
  }
}
