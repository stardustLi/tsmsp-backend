package api.exotic

import models.enums.AdminPermission
import models.fields.MicroServiceToken
import models.types.ExoticMessage

case class CheckAdminPermission(
  secret: MicroServiceToken,
  token: String,
  field: AdminPermission
) extends ExoticMessage
