package api.trace.withPeople

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.IDCard
import services.TraceService.apiGetTracesWithPeople

case class UserGetTraceWithPeopleMessage(userToken: String, idCard: IDCard, startTime: Long, endTime: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiGetTracesWithPeople(userToken, idCard, startTime, endTime, now).get)
  }
}
