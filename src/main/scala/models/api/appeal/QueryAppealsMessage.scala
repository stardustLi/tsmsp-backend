package models.api.appeal

import org.joda.time.DateTime
import scala.util.Try

import models.api.TSMSPMessage
import models.fields.IDCard
import models.{HandleStatus, TSMSPReply}
import service.CodeService.queryAppeal

case class QueryAppealMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, queryAppeal(userToken, idCard, now).get)
  }
}
