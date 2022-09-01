package models.api.user.permission

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.UserService.fetchAllGrantedUsers

import scala.util.Try

case class UserFetchAllGrantedUsersMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, fetchAllGrantedUsers(userToken, now).get)
  }
}
