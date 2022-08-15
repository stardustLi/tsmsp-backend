package Tables


import Globals.GlobalVariables
import Utils.DBUtils
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class UserTraceRow(
                        userName : String,
                        trace : String,
                        time : Long,
                       )

class UserTraceTable(tag : Tag) extends Table[UserTraceRow](tag, GlobalVariables.mainSchema, "user_trace") {
  def userName = column[String]("user_name")
  def trace = column[String]("trace")
  def time = column[Long]("time")
  def * = (userName, trace, time).mapTo[UserTraceRow]

}

object UserTraceTable {
  val userTraceTable = TableQuery[UserTraceTable]
  def addTrace(userName : String, trace : String) : Try[DBIO[Int]] = Try (userTraceTable += UserTraceRow(userName, trace, time = DateTime.now().getMillis))
  def checkTrace(userName : String, startTime : Long, endTime : Long) : Try[List[UserTraceRow]] = Try {
    DBUtils.exec(userTraceTable.filter(ut => ut.userName === userName && ut.time<= endTime && ut.time >= startTime).sortBy(_.time).result).toList
  }
}
