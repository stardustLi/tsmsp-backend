package tables

import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.{UserTrace, UserTraceWithPeople}
import models.fields.IDCard
import utils.db.await

class UserTraceWithPeopleTable(tag: Tag) extends Table[UserTraceWithPeople](tag, mainSchema, "user_trace_with_people") {
  def ThisPeople              = column[IDCard]("this_people")
  def PeopleMetWithThisPeople = column[IDCard]("people_met")
  def time                    = column[Long]("time")
  def *                       = (ThisPeople, PeopleMetWithThisPeople, time).mapTo[UserTraceWithPeople]
}

object UserTraceWithPeopleTableInstance {
  val instance = TableQuery[UserTraceWithPeopleTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Query[UserTraceWithPeopleTable, UserTraceWithPeople, Seq] =
    instance.filter(trace => trace.ThisPeople === idCard)
}
