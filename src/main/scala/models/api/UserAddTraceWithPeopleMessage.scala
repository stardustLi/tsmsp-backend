package models.api

import models.fields.IDCard
import models.{HandleStatus, TSMSPReply, Trace}
import org.joda.time.DateTime
import service.TraceService.addTraceWithPeople

import scala.util.Try

case class UserAddTraceWithPeopleMessage(userToken: String, idCard: IDCard, personIdCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addTraceWithPeople(userToken, idCard, personIdCard, now).get)
  }
}
