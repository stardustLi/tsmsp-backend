package models.api.user.common

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import services.UserService.apiGetProfile

case class UserGetProfileMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiGetProfile(userToken, now).get)
  }
}
