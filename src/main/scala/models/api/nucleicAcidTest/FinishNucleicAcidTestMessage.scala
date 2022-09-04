package models.api.nucleicAcidTest

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, NucleicAcidTestPointName}
import org.joda.time.DateTime
import services.NucleicAcidTestService.finishNucleicAcidTest

import scala.util.Try

case class FinishNucleicAcidTestMessage(userToken: String, idCard: IDCard, testPlace: NucleicAcidTestPointName, nucleicResult: Boolean) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, finishNucleicAcidTest(userToken, idCard, testPlace, nucleicResult, now).get)
  }
}
