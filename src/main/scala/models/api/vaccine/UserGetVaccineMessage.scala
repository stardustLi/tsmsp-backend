package models.api.vaccine

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.TraceService.apiGetTraces
import services.VaccineService.getVaccines

import scala.util.Try

case class UserGetVaccineMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getVaccines(userToken, idCard, now).get)
  }
}
