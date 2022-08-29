package tables

import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.{Trace, UserTrace}
import models.fields.UserName
import utils.db.await

class UserTraceTable(tag: Tag) extends Table[UserTrace](tag, mainSchema, "user_trace") {
  import models.CustomColumnTypes._
  def userName = column[UserName]("user_name")
  def trace    = column[Trace]("trace")
  def time     = column[Long]("time", O.Unique)
  def *        = (userName, trace, time).mapTo[UserTrace]
}

object UserTraceTableInstance {
  val instance = TableQuery[UserTraceTable]
  await(instance.schema.createIfNotExists)

  def filterByUserName(userName: UserName): Try[Query[UserTraceTable, UserTrace, Seq]] = Try {
    instance.filter(trace => trace.userName === userName)
  }
}
