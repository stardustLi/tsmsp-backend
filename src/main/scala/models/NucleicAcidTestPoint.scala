package models

import models.fields.NucleicAcidTestPointName

// 核酸测试点
case class NucleicAcidTestPoint(
  place: DetailedTrace,
  name: NucleicAcidTestPointName
)
