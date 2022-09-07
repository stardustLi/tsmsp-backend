package api.code.jingReport

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAdminPermission
import models.enums.AdminPermission
import models.fields.MicroServiceToken
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class QueryJingReportAll(secret: MicroServiceToken) extends ExoticMessage

case class QueryJingReportsMessage(userToken: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.VIEW_APPEALS)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    QueryJingReportAll(MicroServiceTokens.impl.code)
      .send[TSMSPReply](MicroServicePorts.code.APIUrl)
      .get
  }
}
