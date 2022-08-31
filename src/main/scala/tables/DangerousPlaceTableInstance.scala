package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.enums.RiskLevel
import models.{DangerousPlace, Trace}
import utils.db.await

class DangerousPlaceTable(tag: Tag) extends Table[DangerousPlace](tag, mainSchema, "dangerous_place") {
  import models.types.CustomColumnTypes._
  def place = column[Trace]("place", O.PrimaryKey)
  def level = column[RiskLevel]("level")
  def *     = (place, level).mapTo[DangerousPlace]
}

object DangerousPlaceTableInstance {
  val instance = TableQuery[DangerousPlaceTable]
  await(instance.schema.createIfNotExists)

  def filterByPlace(place: Trace): Query[DangerousPlaceTable, DangerousPlace, Seq] = {
    import models.types.CustomColumnTypes._
    instance.filter(dangerous_place => dangerous_place.place === place)
  }

  def filterByRiskLevel(level: RiskLevel): Query[DangerousPlaceTable, DangerousPlace, Seq] = {
    import models.types.CustomColumnTypes._
    instance.filter(dangerous_place => dangerous_place.level === level)
  }
}
