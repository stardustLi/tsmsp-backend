package models

import models.fields.IDCard

case class Appeal(
  idCard: IDCard,
  reason: String,
  time: Long
)
