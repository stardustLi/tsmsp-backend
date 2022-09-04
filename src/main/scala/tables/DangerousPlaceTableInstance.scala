package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.enums.RiskLevel
import models.fields.TraceID
import models.DangerousPlace
import utils.db.await

class DangerousPlaceTable(tag: Tag) extends Table[DangerousPlace](tag, mainSchema, "dangerous_place") {
  import models.types.CustomColumnTypes._
  def place = column[TraceID]("place", O.PrimaryKey)
  def level = column[RiskLevel]("level")
  def *     = (place, level).mapTo[DangerousPlace]
}

object DangerousPlaceTableInstance {
  val instance: TableQuery[DangerousPlaceTable] = TableQuery[DangerousPlaceTable]
  await(instance.schema.createIfNotExists)

  def filterByPlace(place: TraceID): Query[DangerousPlaceTable, DangerousPlace, Seq] =
    instance.filter(dangerousPlace => dangerousPlace.place === place)

  def filterByRiskLevel(level: RiskLevel): Query[DangerousPlaceTable, DangerousPlace, Seq] = {
    import models.types.CustomColumnTypes._
    instance.filter(dangerousPlace => dangerousPlace.level === level)
  }
}
