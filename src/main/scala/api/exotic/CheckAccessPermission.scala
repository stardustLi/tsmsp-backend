package api.exotic

import models.fields.{IDCard, MicroServiceToken}
import models.types.ExoticMessage

case class CheckAccessPermission(
  secret: MicroServiceToken,
  token: String,
  idCard: IDCard
) extends ExoticMessage
