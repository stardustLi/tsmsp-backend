package models.api.code.appeal

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import services.CodeService.addAppeal

case class UserAppealMessage(userToken: String, idCard: IDCard, reason: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addAppeal(userToken, idCard, reason, now).get)
  }
}
