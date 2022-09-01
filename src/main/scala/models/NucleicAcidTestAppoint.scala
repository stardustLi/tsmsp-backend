package models

import models.fields.IDCard

// 核酸测试预约
case class NucleicAcidTestAppoint(
  idCard: IDCard,
  appointTime: Long,
  testPlace: DetailedTrace
)
