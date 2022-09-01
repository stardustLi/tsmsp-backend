package models

import models.fields.UserName

case class UserAdminPermission (
  userName: UserName,
  admin: Boolean,
  readTraceId: Boolean,
  viewAppeals: Boolean,
  setRiskAreas: Boolean,
  setPolicy: Boolean,
  manageNucleicAcidTestPoints: Boolean
)
