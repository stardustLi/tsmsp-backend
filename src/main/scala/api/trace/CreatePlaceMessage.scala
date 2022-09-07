package api.trace

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic._
import models.enums._
import models.fields._
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class CreatePlace(secret: MicroServiceToken, traceDescriptor: List[String]) extends ExoticMessage

case class CreatePlaceMessage(userToken: String, traceDescriptor: List[String]) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.CREATE_PLACE)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    CreatePlace(MicroServiceTokens.impl.trace, traceDescriptor)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
