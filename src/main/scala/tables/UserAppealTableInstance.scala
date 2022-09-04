package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.Appeal
import models.fields.IDCard
import utils.db.await

class UserAppealTable(tag: Tag) extends Table[Appeal](tag, mainSchema, "user_appeal") {
  def idCard = column[IDCard]("id_card", O.PrimaryKey)
  def reason = column[String]("reason")
  def time   = column[Long]("time")
  def *      = (idCard, reason, time).mapTo[Appeal]
}

object UserAppealTableInstance {
  val instance: TableQuery[UserAppealTable] = TableQuery[UserAppealTable]
  await(instance.schema.createIfNotExists)

  def filterByIdCard(idCard: IDCard): Query[UserAppealTable, Appeal, Seq] =
    instance.filter(Appeal => Appeal.idCard === idCard.toLowerCase())
}
