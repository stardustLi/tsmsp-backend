package models.api.user.permission

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import services.UserService.fetchAllGrantedUsers

case class UserFetchAllGrantedUsersMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, fetchAllGrantedUsers(userToken, now).get)
  }
}
