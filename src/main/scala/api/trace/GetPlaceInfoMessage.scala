package api.trace

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.TraceID
import services.TraceService.getPlaceInfo

case class GetPlaceInfoMessage(traceID: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getPlaceInfo(traceID).get)
  }
}
