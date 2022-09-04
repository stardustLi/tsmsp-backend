package models

import models.fields.{IDCard, NucleicAcidTestPointName}

case class UserNucleicAcidTest(
  idCard: IDCard,
  testPlace: NucleicAcidTestPointName,
  time: Long,
  result: Boolean // 记阳性为 True
)
