package models.api

import org.joda.time.DateTime
import services.UserService.getProfile

import scala.util.Try

case class UserGetProfileMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getProfile(userToken, now).get)
  }
}
