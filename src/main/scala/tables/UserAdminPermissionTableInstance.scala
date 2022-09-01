package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.UserAdminPermission
import models.fields.UserName
import utils.db.await

class UserAdminPermissionTable(tag: Tag) extends Table[UserAdminPermission](tag, mainSchema, "user_admin_permission") {
  def userName                    = column[UserName]("user_name", O.PrimaryKey)
  def admin                       = column[Boolean]("admin")
  def readTraceId                 = column[Boolean]("read_trace_id")
  def viewAppeals                 = column[Boolean]("view_appeals")
  def setRiskAreas                = column[Boolean]("set_risk_areas")
  def setPolicy                   = column[Boolean]("set_policy")
  def manageNucleicAcidTestPoints = column[Boolean]("manage_natps")
  def * = (userName, admin, readTraceId, viewAppeals, setRiskAreas, setPolicy, manageNucleicAcidTestPoints).mapTo[UserAdminPermission]
}

object UserAdminPermissionTableInstance {
  val instance = TableQuery[UserAdminPermissionTable]
  await(instance.schema.createIfNotExists)

  def filterByUserName(userName: UserName): Query[UserAdminPermissionTable, UserAdminPermission, Seq] =
    instance.filter(user => user.userName === userName)

  def all(userName: UserName): UserAdminPermission = UserAdminPermission(
    userName,
    admin = true,
    readTraceId = true,
    viewAppeals = true,
    setRiskAreas = true,
    setPolicy = true,
    manageNucleicAcidTestPoints = true,
  )
}
