package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try
import globals.GlobalVariables.mainSchema
import models.UserPermission
import models.fields.UserName
import utils.db.await

class UserPermissionTable(tag: Tag) extends Table[UserPermission](tag, mainSchema, "user_permission") {
  def userName     = column[UserName]("user_name", O.PrimaryKey)
  def admin        = column[Boolean]("admin")
  def readTraceId  = column[Boolean]("read_trace_id")
  def setRiskAreas = column[Boolean]("set_risk_areas")
  def setPolicy    = column[Boolean]("set_policy")
  def * = (userName, admin, readTraceId, setRiskAreas, setPolicy).mapTo[UserPermission]
}

object UserPermissionTableInstance {
  val instance = TableQuery[UserPermissionTable]
  await(instance.schema.createIfNotExists)

  def filterByUserName(userName: UserName): Try[Query[UserPermissionTable, UserPermission, Seq]] = Try {
    instance.filter(user => user.userName === userName)
  }

  def all(userName: UserName): UserPermission = UserPermission(
    userName,
    admin = true,
    readTraceId = true,
    setRiskAreas = true,
    setPolicy = true
  )
}
