package models.types

import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

import models.enums._

object CustomColumnTypes {
  implicit val riskLevelConverter: JdbcType[RiskLevel] with BaseTypedType[RiskLevel] =
    MappedColumnType.base[RiskLevel, Int](
      riskLevel => riskLevel.value,
      enum => RiskLevel.getType(enum)
    )

  implicit val codeColorConverter: JdbcType[CodeColor] with BaseTypedType[CodeColor] =
    MappedColumnType.base[CodeColor, Int](
      codeColor => codeColor.value,
      enum => CodeColor.getType(enum)
    )

  implicit val traceLevelConverter: JdbcType[TraceLevel] with BaseTypedType[TraceLevel] =
    MappedColumnType.base[TraceLevel, Int](
      traceLevel => traceLevel.value,
      enum => TraceLevel.getType(enum)
    )
}
