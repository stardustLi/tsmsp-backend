package models.api

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}
import models.api.trace._
import models.types.JacksonSerializable
import models.{HandleStatus, TSMSPReply}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[UserAddTraceMessage], name = "UserAddTraceMessage"),
    new JsonSubTypes.Type(value = classOf[UserDeleteTraceMessage], name = "UserDeleteTraceMessage"),
    new JsonSubTypes.Type(value = classOf[UserGetTraceMessage], name = "UserGetTraceMessage"),
    new JsonSubTypes.Type(value = classOf[UserLoginMessage], name = "UserLoginMessage"),
    new JsonSubTypes.Type(value = classOf[UserRegisterMessage], name = "UserRegisterMessage"),
    new JsonSubTypes.Type(value = classOf[UserUpdateTraceMessage], name = "UserUpdateTraceMessage"),
    new JsonSubTypes.Type(value = classOf[PolicyQueryMessage], name = "PolicyQueryMessage"),
    new JsonSubTypes.Type(value = classOf[PolicyUpdateMessage], name = "PolicyUpdateMessage"),
    new JsonSubTypes.Type(value = classOf[UserAppealMessage], name = "UserAppealMessage"),
    new JsonSubTypes.Type(value = classOf[UserAddTraceWithPeopleMessage], name = "UserAddTraceWithPeopleMessage"),
    new JsonSubTypes.Type(value = classOf[SetDangerousPlaceMessage], name = "SetDangerousPlaceMessage"),
    new JsonSubTypes.Type(value = classOf[DangerousPlaceMessage], name = "DangerousPlaceMessage"),
    new JsonSubTypes.Type(value = classOf[SetPermissionMessage], name = "SetPermissionMessage"),
  ))
abstract class TSMSPMessage extends JacksonSerializable {
  def handle(): TSMSPReply = reaction(DateTime.now()) match {
    case Success(value) => value
    case Failure(exception) => TSMSPReply(HandleStatus.ERROR, exception.getMessage)
  }
  def reaction(now: DateTime): Try[TSMSPReply] = Try(TSMSPReply(HandleStatus.OK, "无法识别的消息"))
}
