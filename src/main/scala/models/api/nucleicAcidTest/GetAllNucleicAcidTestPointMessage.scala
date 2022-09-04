package models.api.nucleicAcidTest

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import services.NucleicAcidTestService.getAllNucleicAcidTestPoints

case class GetAllNucleicAcidTestPointMessage() extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getAllNucleicAcidTestPoints().get)
  }
}
