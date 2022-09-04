package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.UserTraceWithPeople
import models.fields.{IDCard, UserName}
import utils.db.await

class UserTraceWithPeopleTable(tag: Tag) extends Table[UserTraceWithPeople](tag, mainSchema, "user_trace_with_people") {
  def ThisPeople = column[IDCard]("this_people")
  def CCUserName = column[UserName]("cc_user_name")
  def CCIDCard   = column[IDCard]("cc_id_card")
  def time       = column[Long]("time", O.Unique)
  def *          = (ThisPeople, CCUserName, CCIDCard, time).mapTo[UserTraceWithPeople]
}

object UserTraceWithPeopleTableInstance {
  val instance: TableQuery[UserTraceWithPeopleTable] = TableQuery[UserTraceWithPeopleTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Query[UserTraceWithPeopleTable, UserTraceWithPeople, Seq] =
    instance.filter(trace => trace.ThisPeople === idCard.toLowerCase())
}
