package models.api.vaccine

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.VaccineService.addVaccine

import scala.util.Try

case class UserAddVaccineMessage(userToken: String, idCard: IDCard, manufacture: String, time: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addVaccine(userToken, idCard, manufacture, time, now).get)
  }
}
