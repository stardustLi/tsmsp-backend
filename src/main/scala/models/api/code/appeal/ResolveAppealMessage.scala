package models.api.code.appeal

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import services.CodeService.resolveAppeal

case class ResolveAppealMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, resolveAppeal(userToken, idCard, now).get)
  }
}
