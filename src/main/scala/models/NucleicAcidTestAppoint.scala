package models

import models.fields.{IDCard, NucleicAcidTestPointName}

// 核酸测试预约
case class NucleicAcidTestAppoint(
  idCard: IDCard,
  testPlace: NucleicAcidTestPointName,
  appointTime: Long
)
