package models

import models.fields.{IDCard, TraceID}

case class UserTrace (
  idCard: IDCard,
  trace: TraceID,
  time: Long
)
