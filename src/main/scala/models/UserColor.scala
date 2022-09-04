package models

import models.enums.CodeColor
import models.fields.IDCard

case class UserColor(
  idCard: IDCard,
  color: CodeColor
)
