package models

import models.enums.RiskLevel
import models.fields.TraceID

case class DangerousPlace (
  place: TraceID,
  level: RiskLevel
)
