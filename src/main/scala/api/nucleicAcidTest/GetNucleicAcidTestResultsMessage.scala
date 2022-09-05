package api.nucleicAcidTest

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.IDCard
import services.NucleicAcidTestService.getNucleicAcidTests

case class GetNucleicAcidTestResultsMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getNucleicAcidTests(userToken, idCard, now).get)
  }
}
