package models.api.user.permission

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.UserService.{apiCheckUserHasAccessByTokenAndIDCard, checkUserHasAccessByTokenAndIDCard}

import scala.util.Try

case class UserWhetherGrantedMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).get)
  }
}
