package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.UserPermission
import models.fields.UserName
import utils.db.await

class UserPermissionTable(tag: Tag) extends Table[UserPermission](tag, mainSchema, "user_permission") {
  def userName     = column[UserName]("user_name", O.PrimaryKey)
  def admin        = column[Boolean]("admin")
  def readTraceId  = column[Boolean]("read_trace_id")
  def viewAppeals  = column[Boolean]("view_appeals")
  def setRiskAreas = column[Boolean]("set_risk_areas")
  def setPolicy    = column[Boolean]("set_policy")
  def * = (userName, admin, readTraceId, viewAppeals, setRiskAreas, setPolicy).mapTo[UserPermission]
}

object UserPermissionTableInstance {
  val instance = TableQuery[UserPermissionTable]
  await(instance.schema.createIfNotExists)

  def filterByUserName(userName: UserName): Query[UserPermissionTable, UserPermission, Seq] =
    instance.filter(user => user.userName === userName)

  def all(userName: UserName): UserPermission = UserPermission(
    userName,
    admin = true,
    readTraceId = true,
    viewAppeals = true,
    setRiskAreas = true,
    setPolicy = true
  )
}
