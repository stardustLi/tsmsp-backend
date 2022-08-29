package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.UserTraceWithPeople
import models.fields.IDCard
import utils.db.await

class UserTraceWithPeopleTable(tag: Tag) extends Table[UserTraceWithPeople](tag, mainSchema, "user_trace_with_people") {
  def ThisPeople = column[IDCard]("id_card")
  def PeopleMetWithThisPeople = column[IDCard]("trace")
  def time = column[Long]("time")
  def * = (ThisPeople, PeopleMetWithThisPeople, time).mapTo[UserTraceWithPeople]
}

object UserTraceWithPeopleTableInstance {
  val instance = TableQuery[UserTraceWithPeopleTable]
  await(instance.schema.createIfNotExists)
/*
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
*/
}