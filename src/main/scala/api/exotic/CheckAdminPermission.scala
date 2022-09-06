package api.exotic

import models.enums.AdminPermission
import models.fields.MicroServiceToken
import models.types.JacksonSerializable

case class CheckAdminPermission(
  secret: MicroServiceToken,
  token: String,
  field: AdminPermission,
  `type`: String = "CheckAdminPermission"
) extends JacksonSerializable
