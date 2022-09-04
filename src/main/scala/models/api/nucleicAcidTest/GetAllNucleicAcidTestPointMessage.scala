package models.api.nucleicAcidTest

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.NucleicAcidTestService.getAllNucleicAcidTestPoints

import scala.util.Try

case class GetAllNucleicAcidTestPointMessage() extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getAllNucleicAcidTestPoints().get)
  }
}
