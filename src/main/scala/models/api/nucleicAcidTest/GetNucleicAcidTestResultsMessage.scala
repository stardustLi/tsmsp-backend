package models.api.nucleicAcidTest

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, NucleicAcidTestPointName}
import org.joda.time.DateTime
import services.NucleicAcidTestService.{getNucleicAcidTests, queryWaitingPerson}

import scala.util.Try

case class GetNucleicAcidTestResultsMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getNucleicAcidTests(userToken, idCard, now).get)
  }
}
