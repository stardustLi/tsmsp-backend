package models.api.trace.withPeople

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, UserName}
import services.TraceService.addTraceWithPeople

case class UserAddTraceWithPeopleMessage(userToken: String, idCard: IDCard, cc: UserName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addTraceWithPeople(userToken, idCard, cc, now).get)
  }
}
