package models.api.trace

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.TraceService.createPlace

import scala.util.Try

case class CreatePlaceMessage(userToken: String, traceDescriptor: List[String]) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, createPlace(userToken, traceDescriptor, now).get)
  }
}
