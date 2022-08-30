package models.api.appeal

import org.joda.time.DateTime
import scala.util.Try

import models.api.TSMSPMessage
import models.fields.IDCard
import models.{HandleStatus, TSMSPReply}
import services.CodeService.addAppeal

case class UserAppealMessage(userToken: String, idCard: IDCard, reason: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addAppeal(userToken, idCard, reason, now).get)
  }
}
