package api.nucleicAcidTest

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.{IDCard, NucleicAcidTestPointName}
import services.NucleicAcidTestService.finishNucleicAcidTest

case class FinishNucleicAcidTestMessage(userToken: String, idCard: IDCard, testPlace: NucleicAcidTestPointName, nucleicResult: Boolean) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, finishNucleicAcidTest(userToken, idCard, testPlace, nucleicResult, now).get)
  }
}
