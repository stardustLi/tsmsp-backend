package tables

import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.enums.RiskLevel
import models.{DangerousPlace, Trace}
import utils.db.await


class DangerousPlaceTable(tag: Tag) extends Table[DangerousPlace](tag, mainSchema, "dangerous_place") {
  import models.CustomColumnTypes._
  def place = column[Trace]("place")
  def level = column[RiskLevel]("level")
  def * = (place, level).mapTo[DangerousPlace]
}

object DangerousPlaceTableInstance {
  val instance = TableQuery[DangerousPlaceTable]
  await(instance.schema.createIfNotExists)

  def filterByTrace(place: Trace): Try[Query[DangerousPlaceTable, DangerousPlace, Seq]] = Try {
    import models.CustomColumnTypes._
    instance.filter(dangerous_place => dangerous_place.place === place)
  }

  def filterByRiskLevel(level: RiskLevel): Try[Query[DangerousPlaceTable, DangerousPlace, Seq]] = Try {
    instance.filter(dangerous_place => dangerous_place.level === level)
  }
}
