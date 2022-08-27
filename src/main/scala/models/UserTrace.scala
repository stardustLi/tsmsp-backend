package models

import models.fields.UserName

case class UserTrace (
  userName: UserName,
  trace: Trace,
  time: Long,
)
