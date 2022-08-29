package tables

import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.{Appeal, User}
import models.fields.{IDCard, UserName}
import utils.db.await

class UserAppealTable(tag: Tag) extends Table[User](tag, mainSchema, "user") {
  def realName = column[String]("real_name")
  def idCard   = column[IDCard]("id_card", O.Unique)
  def reason   = column[String]("reason")
  def *        = (realName, idCard, reason).mapTo[Appeal]
}

object UserAppealTableInstance {
  val instance = TableQuery[UserAppealTable]
  await(instance.schema.createIfNotExists)

  def filterByIdCard(idCard: String): Try[Query[UserAppealTable, Appeal, Seq]] = Try {
    instance.filter(Appeal => Appeal.idCard === idCard)
  }
}
