package tables

import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.{Policy, Trace, UserTrace}
import models.fields.UserName
import utils.db.await

class PolicyTable(tag: Tag) extends Table[Policy](tag, mainSchema, "policy") {
  import models.CustomColumnTypes._

  def place    = column[Trace]("trace", O.PrimaryKey)
  def contents = column[String]("time")
  def *        = (place, contents).mapTo[Policy]
}

object PolicyTableInstance {
  val instance = TableQuery[PolicyTable]
  await(instance.schema.createIfNotExists)

  def filterByPlace(place: Trace): Query[PolicyTable, Policy, Seq] = {
    import models.CustomColumnTypes._
    instance.filter(policy => policy.place === place)
  }
}
