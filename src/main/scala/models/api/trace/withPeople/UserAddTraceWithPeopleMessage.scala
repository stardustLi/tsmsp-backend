package models.api.trace.withPeople

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.TraceService.addTraceWithPeople

import scala.util.Try

case class UserAddTraceWithPeopleMessage(userToken: String, idCard: IDCard, personIdCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addTraceWithPeople(userToken, idCard, personIdCard, now).get)
  }
}




//case class UserAddTraceMessage(userToken: String, idCard: IDCard, trace: Trace) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, addTrace(userToken, idCard, trace, now).get)
//  }
//}
