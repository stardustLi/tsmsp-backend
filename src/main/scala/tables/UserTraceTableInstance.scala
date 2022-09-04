package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.UserTrace
import models.fields.{IDCard, TraceID}
import utils.db.await

class UserTraceTable(tag: Tag) extends Table[UserTrace](tag, mainSchema, "user_trace") {
  import models.types.CustomColumnTypes._
  def idCard = column[IDCard]("id_card")
  def trace  = column[TraceID]("trace")
  def time   = column[Long]("time", O.Unique)
  def *      = (idCard, trace, time).mapTo[UserTrace]
}

object UserTraceTableInstance {
  val instance: TableQuery[UserTraceTable] = TableQuery[UserTraceTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Query[UserTraceTable, UserTrace, Seq] =
    instance.filter(trace => trace.idCard === idCard.toLowerCase())
}
