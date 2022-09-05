package api.trace

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.TraceID
import services.TraceService.getPlaceSubordinates

case class GetPlaceSubordinatesMessage(traceID: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getPlaceSubordinates(traceID).get)
  }
}
