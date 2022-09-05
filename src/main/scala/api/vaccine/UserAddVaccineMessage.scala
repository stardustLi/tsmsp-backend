package api.vaccine

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.IDCard
import services.VaccineService.addVaccine

case class UserAddVaccineMessage(userToken: String, idCard: IDCard, manufacture: String, time: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addVaccine(userToken, idCard, manufacture, time, now).get)
  }
}
