package api.code

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.IDCard
import services.CodeService.jingReport

case class JingReportMessage(userToken: String, idCard: IDCard, reason: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, jingReport(userToken, idCard, reason, now).get)
  }
}
