package models

import models.fields.TraceID

case class Policy(
  place: TraceID,
  contents: String
)
