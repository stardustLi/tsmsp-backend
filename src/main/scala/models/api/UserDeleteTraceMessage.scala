package models.api

import models.{HandleStatus, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import service.TraceService.removeTrace

case class UserDeleteTraceMessage(userToken: String, time: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, removeTrace(userToken, time, now).get)
  }
}
