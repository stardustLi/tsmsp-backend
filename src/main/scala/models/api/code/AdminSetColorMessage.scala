package models.api.code

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.enums.CodeColor
import models.fields.IDCard
import services.CodeService.adminSetColor

case class AdminSetColorMessage(userToken: String, idCard: IDCard, color: CodeColor) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, adminSetColor(userToken, idCard, color, now).get)
  }
}
