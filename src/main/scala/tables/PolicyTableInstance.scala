package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.fields.TraceID
import models.Policy
import utils.db.await

class PolicyTable(tag: Tag) extends Table[Policy](tag, mainSchema, "policy") {
  def place    = column[TraceID]("trace", O.PrimaryKey)
  def contents = column[String]("time")
  def *        = (place, contents).mapTo[Policy]
}

object PolicyTableInstance {
  val instance: TableQuery[PolicyTable] = TableQuery[PolicyTable]
  await(instance.schema.createIfNotExists)

  def filterByPlace(place: TraceID): Query[PolicyTable, Policy, Seq] =
    instance.filter(policy => policy.place === place)

  def filterByPlaces(places: Seq[TraceID]): Query[PolicyTable, Policy, Seq] =
    instance.filter(policy => policy.place.inSet(places))
}
