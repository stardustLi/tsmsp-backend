package models

import models.fields.{NucleicAcidTestPointName, TraceID}

// 核酸测试点
case class NucleicAcidTestPoint(
  place: TraceID,
  name: NucleicAcidTestPointName
)
