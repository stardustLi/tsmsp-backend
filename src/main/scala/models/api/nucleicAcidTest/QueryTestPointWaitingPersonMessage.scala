package models.api.nucleicAcidTest

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.NucleicAcidTestPointName
import org.joda.time.DateTime
import services.NucleicAcidTestService.queryWaitingPersonNumber

import scala.util.Try

case class QueryTestPointWaitingPersonMessage(place: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, queryWaitingPersonNumber(place).get)
  }
}
