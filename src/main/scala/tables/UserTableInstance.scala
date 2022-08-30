package tables

import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.User
import models.fields.{IDCard, UserName}
import utils.db.await

class UserTable(tag: Tag) extends Table[User](tag, mainSchema, "user") {
  def userName = column[UserName]("user_name", O.PrimaryKey)
  def password = column[String]("password")
  def realName = column[String]("real_name")
  def idCard   = column[IDCard]("id_card", O.Unique)
  def *        = (userName, password, realName, idCard).mapTo[User]
}

object UserTableInstance {
  val instance = TableQuery[UserTable]
  await(instance.schema.createIfNotExists)

  def filterByUserName(userName: UserName): Query[UserTable, User, Seq] =
    instance.filter(user => user.userName === userName)

  def filterByUserPass(userName: UserName, password: String): Query[UserTable, User, Seq] =
    instance.filter(user => user.userName === userName && user.password === password)
}
