package models

import models.enums.TraceLevel
import models.fields.TraceID

case class TraceTree(
  id: TraceID,
  name: String,
  level: TraceLevel,
  parentID: TraceID
)
