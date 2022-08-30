package models

import models.fields.UserName

case class UserPermission (
  userName: UserName,
  admin: Boolean,
  readTraceId: Boolean,
  setRiskAreas: Boolean,
  setPolicy: Boolean
)
