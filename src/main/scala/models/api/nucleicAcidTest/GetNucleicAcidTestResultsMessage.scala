package models.api.nucleicAcidTest

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, NucleicAcidTestPointName}
import services.NucleicAcidTestService.{getNucleicAcidTests, queryWaitingPerson}

case class GetNucleicAcidTestResultsMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getNucleicAcidTests(userToken, idCard, now).get)
  }
}
