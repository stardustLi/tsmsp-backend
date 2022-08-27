package models

import models.enums.RiskLevel

case class DangerousPlace (
  place: Trace,
  level: RiskLevel
)
