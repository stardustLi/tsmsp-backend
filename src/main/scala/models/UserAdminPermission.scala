package models

import models.fields.UserName

case class UserAdminPermission (
  userName: UserName,
  admin: Boolean,
  createPlace: Boolean,
  readTraceId: Boolean,
  viewAppeals: Boolean,
  setRiskAreas: Boolean,
  setPolicy: Boolean,
  manageNucleicAcidTestPoints: Boolean,
  finishNucleicAcidTest: Boolean,
  assignColor: Boolean
)
