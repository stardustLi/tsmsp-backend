package models

import models.fields.IDCard

case class JingReport(
  idCard: IDCard,
  reason: String,
  time: Long
)
