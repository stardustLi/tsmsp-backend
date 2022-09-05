package api.user.common

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.{Password, UserName}
import utils.network.send

case class UserLoginMessage(userName: UserName, password: Password) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    send(new Login(userName, password))
  }
}
