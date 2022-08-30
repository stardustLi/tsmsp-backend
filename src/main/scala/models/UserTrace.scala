package models

import models.fields.IDCard

case class UserTrace (
  idCard: IDCard,
  trace: Trace,
  time: Long
)
