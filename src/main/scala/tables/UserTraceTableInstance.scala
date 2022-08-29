package tables

import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.{Trace, UserTrace}
import models.fields.IDCard
import utils.db.await

class UserTraceTable(tag: Tag) extends Table[UserTrace](tag, mainSchema, "user_trace") {
  import models.CustomColumnTypes._
  def idCard = column[IDCard]("id_card")
  def trace  = column[Trace]("trace")
  def time   = column[Long]("time", O.Unique)
  def *      = (idCard, trace, time).mapTo[UserTrace]
}

object UserTraceTableInstance {
  val instance = TableQuery[UserTraceTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Try[Query[UserTraceTable, UserTrace, Seq]] = Try {
    instance.filter(trace => trace.idCard === idCard)
  }
}
