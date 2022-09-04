package models.api.nucleicAcidTest

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, NucleicAcidTestPointName}
import services.NucleicAcidTestService.appointNucleicAcidTest

case class AppointNucleicAcidTestMessage(userToken: String, idCard: IDCard, testPlace: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, appointNucleicAcidTest(userToken, idCard, testPlace, now).get)
  }
}
