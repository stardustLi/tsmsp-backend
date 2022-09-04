package models.api.trace

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.TraceID
import services.TraceService.getPlaceInfo

case class GetPlaceInfoMessage(traceID: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getPlaceInfo(traceID).get)
  }
}
