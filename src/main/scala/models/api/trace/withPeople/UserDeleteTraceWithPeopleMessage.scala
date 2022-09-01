package models.api.trace.withPeople

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import services.TraceService.removeTraceWithPeople

case class UserDeleteTraceWithPeopleMessage(userToken: String, idCard: IDCard, time: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, removeTraceWithPeople(userToken, idCard, time, now).get)
  }
}
