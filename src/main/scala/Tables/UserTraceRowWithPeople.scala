package Tables
import Globals.GlobalVariables
import Utils.DBUtils
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try


case class UserTraceRowWithPeople(
  ThisPeople: String,//本人身份证号
  PeopleMetWithThisPeople: String,//密接者身份证号
  time: Long,
)
//

class UserTraceTableWithPeople(tag: Tag) extends Table[UserTraceRowWithPeople](tag, GlobalVariables.mainSchema, "user_trace_with_people") {
  def ThisPeople = column[String]("id_card")
  def PeopleMetWithThisPeople = column[String]("trace")
  def time = column[Long]("time")
  def * = (ThisPeople, PeopleMetWithThisPeople, time).mapTo[UserTraceRowWithPeople]
}

object UserTraceTableWithPeople {
  val userTraceTableWithPeople = TableQuery[UserTraceTableWithPeople]

  def addTrace(ThisPeople: String, PeopleMetWithThisPeople: String, time: Long): Try[DBIO[Int]] = Try {
    userTraceTableWithPeople += UserTraceRowWithPeople(ThisPeople, PeopleMetWithThisPeople, time)
  }

  def checkTrace(ThisPeople: String, startTime: Long, endTime: Long, PeopleMetWithThisPeople: String): Try[List[UserTraceRowWithPeople]] = Try {
    DBUtils.exec(
      userTraceTableWithPeople
        .filter(
          PeopleMetWithThisPeople => PeopleMetWithThisPeople.ThisPeople === ThisPeople && PeopleMetWithThisPeople.time.between(startTime, endTime)
        )
        .sortBy(trace => trace.time)
        .result
    ).toList
  }
}