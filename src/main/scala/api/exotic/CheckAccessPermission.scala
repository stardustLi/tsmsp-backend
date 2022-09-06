package api.exotic

import models.fields.{IDCard, MicroServiceToken}
import models.types.JacksonSerializable

case class CheckAccessPermission(
  secret: MicroServiceToken,
  token: String,
  idCard: IDCard,
  `type`: String = "CheckAccessPermission"
) extends JacksonSerializable
