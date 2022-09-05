package api.user.permission

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import services.UserService.fetchAllGrantedUsers

case class UserFetchAllGrantedUsersMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, fetchAllGrantedUsers(userToken, now).get)
  }
}
