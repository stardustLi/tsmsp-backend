package models

import models.enums.RiskLevel
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._
import utils.IOUtils

object CustomColumnTypes {
  implicit val traceConverter: JdbcType[Trace] with BaseTypedType[Trace] = {
    MappedColumnType.base[Trace, String](
      trace => IOUtils.serialize(trace).get,
      json => IOUtils.deserialize[Trace](json).get
    )
  }

  implicit val riskLevelConverter: JdbcType[RiskLevel] with BaseTypedType[RiskLevel] = {
    MappedColumnType.base[RiskLevel, Int](
      riskLevel => riskLevel.value,
      enum => RiskLevel.getType(enum)
    )
  }
}
