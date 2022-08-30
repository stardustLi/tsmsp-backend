package models

import models.fields.IDCard

case class UserNucleicAcidTest(
  idCard: IDCard,
  time: Long,
  result: Boolean //记阳性为True
)
