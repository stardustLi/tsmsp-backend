package models

import models.fields.{IDCard}

case class Appeal(
  realName: String,
  idCard: String,
  reason: String
)
