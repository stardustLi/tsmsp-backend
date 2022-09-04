package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.UserOthersQuery
import models.fields.{IDCard, UserName}
import utils.db.await

class UserOthersQueryTable(tag: Tag) extends Table[UserOthersQuery](tag, mainSchema, "user_others_query") {
  def userName     = column[UserName]("user_name")
  def idCardOthers = column[IDCard]("id_card_others")
  def *            = (userName, idCardOthers).mapTo[UserOthersQuery]
}

object UserOthersQueryTableInstance {
  val instance: TableQuery[UserOthersQueryTable] = TableQuery[UserOthersQueryTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCardOthers: IDCard): Query[UserOthersQueryTable, UserOthersQuery, Seq] =
    instance.filter(user => user.idCardOthers === idCardOthers.toLowerCase())

  def filterByUserIDCard(userName: UserName, idCardOthers: IDCard): Query[UserOthersQueryTable, UserOthersQuery, Seq] =
    instance.filter(user => user.userName === userName && user.idCardOthers === idCardOthers.toLowerCase())
}
