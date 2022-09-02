package models.api.acid

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.NucleicAcidTestService.addNucleicAcidTest

import scala.util.Try

case class UserAddAcidTestMessage(userToken: String, idCard: IDCard, now: DateTime, result: Boolean) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addNucleicAcidTest(userToken: String, idCard: IDCard, now: DateTime, result: Boolean).get)
  }
}
