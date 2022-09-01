package models.api.acid

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.NucleicAcidTestService.getNucleicAcidTests
import services.TraceService.getTraces
import services.VaccineService.getVaccines

import scala.util.Try

case class UserGetAcidMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getNucleicAcidTests(userToken, idCard, now).get)
  }
}
