package models

import models.fields.{UserName, IDCard}

case class User (
  userName: UserName,
  password: String,
  realName: String,
  idCard: IDCard
)
