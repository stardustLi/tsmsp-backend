package api.nucleicAcidTest

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.TraceID
import org.joda.time.DateTime

import scala.util.Try

case class GetAllNucleicAcidTestPointMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getAllNucleicAcidTestPoints(place).get)
  }
}
