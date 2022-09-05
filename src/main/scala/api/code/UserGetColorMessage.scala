package api.code

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import scala.util.Try
import models.fields.IDCard
import services.CodeService.getColor

case class UserGetColorMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getColor(userToken, idCard, now).get)
  }
}
