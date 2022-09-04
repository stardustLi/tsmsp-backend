package models.api.nucleicAcidTest

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.NucleicAcidTestPointName
import services.NucleicAcidTestService.queryWaitingPersonNumber

case class QueryTestPointWaitingPersonMessage(place: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, queryWaitingPersonNumber(place).get)
  }
}