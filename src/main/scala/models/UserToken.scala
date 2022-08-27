package models

import models.fields.UserName

case class UserToken (
  userName: UserName,
  token: String,
  refreshTime: Long
)
