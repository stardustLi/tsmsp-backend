package models.api.trace

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import services.TraceService.createPlace

case class CreatePlaceMessage(userToken: String, traceDescriptor: List[String]) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, createPlace(userToken, traceDescriptor, now).get)
  }
}
