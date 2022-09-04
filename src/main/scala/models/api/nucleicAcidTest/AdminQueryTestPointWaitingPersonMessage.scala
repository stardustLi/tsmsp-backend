package models.api.nucleicAcidTest

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.NucleicAcidTestPointName
import services.NucleicAcidTestService.queryWaitingPerson

case class AdminQueryTestPointWaitingPersonMessage(userToken: String, place: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, queryWaitingPerson(userToken, place, now).get)
  }
}
