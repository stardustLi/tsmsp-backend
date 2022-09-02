package models.api.acid

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.NucleicAcidTestService.getPositiveTestPeople

import scala.util.Try

case class GetPositiveTestPeopleMessage(result: Boolean) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getPositiveTestPeople(result).get)
  }
}
