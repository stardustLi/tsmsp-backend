package models

import models.enums.TraceLevel

case class TraceTree(
  id: Int,
  name: String,
  level: TraceLevel,
  parentId: Int
)
