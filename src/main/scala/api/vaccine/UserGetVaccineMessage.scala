package api.vaccine

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.IDCard
import services.VaccineService.getVaccines

case class UserGetVaccineMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getVaccines(userToken, idCard, now).get)
  }
}
