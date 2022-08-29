package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}
import scala.util.Try

import globals.GlobalVariables.mainSchema
import models.UserPermission
import utils.db.await

class UserPermissionTable(tag: Tag) extends Table[UserPermission](tag, mainSchema, "user_permission") {
  def userName     = column[String]("user_name", O.PrimaryKey)
  def admin        = column[Boolean]("admin")
  def readTraceId  = column[Boolean]("read_trace_id")
  def setRiskAreas = column[Boolean]("set_risk_areas")
  def * = (userName, admin, readTraceId, setRiskAreas).mapTo[UserPermission]
}

object UserPermissionTableInstance {
  val instance = TableQuery[UserPermissionTable]
  await(instance.schema.createIfNotExists)

  def filterByUserName(userName: String): Try[Query[UserPermissionTable, UserPermission, Seq]] = Try {
    instance.filter(user => user.userName === userName)
  }
}
