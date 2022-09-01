package models.api.trace.withPeople

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.TraceService.apiGetTracesWithPeopleWithIDCard

import scala.util.Try

case class UserGetTraceWithPeopleWithIDCardMessage(userToken: String, idCard: IDCard, startTime: Long, endTime: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiGetTracesWithPeopleWithIDCard(userToken, idCard, startTime, endTime, now).get)
  }
}
