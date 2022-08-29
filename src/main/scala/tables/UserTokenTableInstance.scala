package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}
import scala.util.Try

import globals.GlobalVariables.mainSchema
import models.UserToken
import models.fields.UserName
import utils.db.await

class UserTokenTable(tag: Tag) extends Table[UserToken](tag, mainSchema, "user_token") {
  def userName    = column[UserName]("user_name", O.PrimaryKey)
  def token       = column[String]("token", O.Unique)
  def refreshTime = column[Long]("refresh_time")
  def *           = (userName, token, refreshTime).mapTo[UserToken]
}

object UserTokenTableInstance {
  val instance = TableQuery[UserTokenTable]
  await(instance.schema.createIfNotExists)

  def filterByUserName(userName: UserName): Try[Query[UserTokenTable, UserToken, Seq]] = Try {
    instance.filter(user => user.userName === userName)
  }
}