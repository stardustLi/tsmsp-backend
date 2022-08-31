package models

import models.fields.{IDCard, Password, UserName}

case class User (
  userName: UserName,
  password: Password,
  realName: String,
  idCard: IDCard
)
