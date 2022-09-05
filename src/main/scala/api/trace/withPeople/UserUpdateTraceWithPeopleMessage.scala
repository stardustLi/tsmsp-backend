package api.trace.withPeople

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.{IDCard, UserName}
import services.TraceService.updateTraceWithPeople

case class UserUpdateTraceWithPeopleMessage(userToken: String, idCard: IDCard, time: Long, cc: UserName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, updateTraceWithPeople(userToken, idCard, time, cc, now).get)
  }
}
