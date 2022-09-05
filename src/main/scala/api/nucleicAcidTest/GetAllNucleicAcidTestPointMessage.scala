package api.nucleicAcidTest

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import services.NucleicAcidTestService.getAllNucleicAcidTestPoints

case class GetAllNucleicAcidTestPointMessage() extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getAllNucleicAcidTestPoints().get)
  }
}
