package models.api.nucleicAcidTest

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.TraceID
import services.NucleicAcidTestService.getAllNucleicAcidTestPoints

case class GetAllNucleicAcidTestPointMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getAllNucleicAcidTestPoints(place).get)
  }
}
