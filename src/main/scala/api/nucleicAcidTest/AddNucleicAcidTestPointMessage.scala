package api.nucleicAcidTest

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.{NucleicAcidTestPointName, TraceID}
import services.NucleicAcidTestService.addNucleicAcidTestPoint

case class AddNucleicAcidTestPointMessage(userToken: String, place: TraceID, name: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addNucleicAcidTestPoint(userToken, place, name, now).get)
  }
}
