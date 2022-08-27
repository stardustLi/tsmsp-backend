package models

case class UserPermission (
  userName: String,
  admin: Boolean,
  readTraceId: Boolean,
  setRiskAreas: Boolean
)
