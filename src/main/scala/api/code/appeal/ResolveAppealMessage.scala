package api.code.appeal

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import scala.util.Try
import models.fields.IDCard
import services.CodeService.resolveAppeal

case class ResolveAppealMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, resolveAppeal(userToken, idCard, now).get)
  }
}
