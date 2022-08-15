package Tables

import Globals.GlobalVariables
import Utils.DBUtils
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class UserRow(
                    userName : String,
                    password : String,
                    realName : String
                  )

class UserTable(tag : Tag) extends Table[UserRow](tag, GlobalVariables.mainSchema, "user") {
  def userName = column[String]("user_name", O.PrimaryKey)
  def password = column[String]("password")
  def realName = column[String]("real_name")
  def * = (userName, password, realName).mapTo[UserRow]
}

object UserTable {
  val userTable = TableQuery[UserTable]

  def addUser(userName: String, password: String, realName: String): Try[DBIO[Int]] = Try(userTable += UserRow(userName, password, realName))

  def checkUserExists(userName: String): Try[Boolean] = Try(DBUtils.exec(userTable.filter(_.userName === userName).size.result) > 0)

  def checkPassword(userName: String, password: String): Try[Boolean] = Try(DBUtils.exec(userTable.filter(u => u.userName === userName && u.password === password).size.result) > 0)

}