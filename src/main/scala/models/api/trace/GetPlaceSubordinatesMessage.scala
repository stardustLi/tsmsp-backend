package models.api.trace

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.TraceID
import services.TraceService.getPlaceSubordinates

case class GetPlaceSubordinatesMessage(traceID: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getPlaceSubordinates(traceID).get)
  }
}
