package models.api.user.common

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.CodeService.getColor

import scala.util.Try

case class UserGetColorMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getColor(userToken, now).get)
  }
}
