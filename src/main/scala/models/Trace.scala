package models

import models.enums.TraceLevel
import models.fields.TraceID

class Trace(val id: TraceID, val name: String, val level: TraceLevel) {
  var parent: Option[Trace] = None
}
