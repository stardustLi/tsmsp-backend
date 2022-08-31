package models.types

import models.enums.RiskLevel
import models.{DetailedTrace, Trace}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._
import utils.io

object CustomColumnTypes {
  implicit val traceConverter: JdbcType[Trace] with BaseTypedType[Trace] =
    MappedColumnType.base[Trace, String](
      trace => io.serialize(trace).get,
      json => io.deserialize[Trace](json).get
    )

  implicit val riskLevelConverter: JdbcType[RiskLevel] with BaseTypedType[RiskLevel] =
    MappedColumnType.base[RiskLevel, Int](
      riskLevel => riskLevel.value,
      enum => RiskLevel.getType(enum)
    )

  implicit val detailedTraceConverter: JdbcType[DetailedTrace] with BaseTypedType[DetailedTrace] =
    MappedColumnType.base[DetailedTrace, String](
      detailed_Trace => io.serialize(detailed_Trace).get,
      json => io.deserialize[DetailedTrace](json).get
    )
}
