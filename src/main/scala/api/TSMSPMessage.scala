package api

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

import scala.util.{Failure, Success, Try}
import api.code._
import api.code.appeal._
import api.nucleicAcidTest._
import api.policy._
import api.trace._
import api.trace.common._
import api.trace.withPeople._
import api.user.admin._
import api.user.common._
import api.user.permission._
import api.vaccine._
import models.types.{HandleStatus, JacksonSerializable, TSMSPReply}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    // user.common
    new JsonSubTypes.Type(value = classOf[UserLoginMessage], name = "UserLoginMessage"),
    new JsonSubTypes.Type(value = classOf[UserRegisterMessage], name = "UserRegisterMessage"),
    new JsonSubTypes.Type(value = classOf[UserGetProfileMessage], name = "UserGetProfileMessage"),
    new JsonSubTypes.Type(value = classOf[UserChangePasswordMessage], name = "UserChangePasswordMessage"),
    // user.admin
    new JsonSubTypes.Type(value = classOf[GetAdminPermissionMessage], name = "GetAdminPermissionMessage"),
    new JsonSubTypes.Type(value = classOf[SetAdminPermissionMessage], name = "SetAdminPermissionMessage"),
    // user.permission
    new JsonSubTypes.Type(value = classOf[UserFetchAllGrantedUsersMessage], name = "UserFetchAllGrantedUsersMessage"),
    new JsonSubTypes.Type(value = classOf[UserGrantPermissionMessage], name = "UserGrantPermissionMessage"),
    new JsonSubTypes.Type(value = classOf[UserRevokePermissionMessage], name = "UserRevokePermissionMessage"),
    // trace
    new JsonSubTypes.Type(value = classOf[CreatePlaceMessage], name = "CreatePlaceMessage"),
    new JsonSubTypes.Type(value = classOf[GetPlaceInfoMessage], name = "GetPlaceInfoMessage"),
    new JsonSubTypes.Type(value = classOf[GetPlaceSubordinatesMessage], name = "GetPlaceSubordinatesMessage"),
    // trace.common
    new JsonSubTypes.Type(value = classOf[UserAddTraceMessage], name = "UserAddTraceMessage"),
    new JsonSubTypes.Type(value = classOf[UserDeleteTraceMessage], name = "UserDeleteTraceMessage"),
    new JsonSubTypes.Type(value = classOf[UserUpdateTraceMessage], name = "UserUpdateTraceMessage"),
    new JsonSubTypes.Type(value = classOf[UserGetTraceMessage], name = "UserGetTraceMessage"),
    // trace.withPeople
    new JsonSubTypes.Type(value = classOf[UserAddTraceWithPeopleMessage], name = "UserAddTraceWithPeopleMessage"),
    new JsonSubTypes.Type(value = classOf[UserDeleteTraceWithPeopleMessage], name = "UserDeleteTraceWithPeopleMessage"),
    new JsonSubTypes.Type(value = classOf[UserUpdateTraceWithPeopleMessage], name = "UserUpdateTraceWithPeopleMessage"),
    new JsonSubTypes.Type(value = classOf[UserGetTraceWithPeopleMessage], name = "UserGetTraceWithPeopleMessage"),
    new JsonSubTypes.Type(value = classOf[UserGetTraceWithPeopleWithIDCardMessage], name = "UserGetTraceWithPeopleWithIDCardMessage"),
    // policy
    new JsonSubTypes.Type(value = classOf[PolicyQueryMessage], name = "PolicyQueryMessage"),
    new JsonSubTypes.Type(value = classOf[PolicyUpdateMessage], name = "PolicyUpdateMessage"),
    // code
    new JsonSubTypes.Type(value = classOf[AdminSetColorMessage], name = "AdminSetColorMessage"),
    new JsonSubTypes.Type(value = classOf[DangerousPlaceMessage], name = "DangerousPlaceMessage"),
    new JsonSubTypes.Type(value = classOf[JingReportMessage], name = "JingReportMessage"),
    new JsonSubTypes.Type(value = classOf[SetDangerousPlaceMessage], name = "SetDangerousPlaceMessage"),
    new JsonSubTypes.Type(value = classOf[UserGetColorMessage], name = "UserGetColorMessage"),
    // code.appeal
    new JsonSubTypes.Type(value = classOf[QueryAppealMessage], name = "QueryAppealMessage"),
    new JsonSubTypes.Type(value = classOf[ResolveAppealMessage], name = "ResolveAppealMessage"),
    new JsonSubTypes.Type(value = classOf[UserAppealMessage], name = "UserAppealMessage"),
    // vaccine
    new JsonSubTypes.Type(value = classOf[UserAddVaccineMessage], name = "UserAddVaccineMessage"),
    new JsonSubTypes.Type(value = classOf[UserGetVaccineMessage], name = "UserGetVaccineMessage"),
    // nucleicAcidTest
    new JsonSubTypes.Type(value = classOf[AddNucleicAcidTestPointMessage], name = "AddNucleicAcidTestPointMessage"),
    new JsonSubTypes.Type(value = classOf[AdminQueryTestPointWaitingPersonMessage], name = "AdminQueryTestPointWaitingPersonMessage"),
    new JsonSubTypes.Type(value = classOf[AppointNucleicAcidTestMessage], name = "AppointNucleicAcidTestMessage"),
    new JsonSubTypes.Type(value = classOf[FinishNucleicAcidTestMessage], name = "FinishNucleicAcidTestMessage"),
    new JsonSubTypes.Type(value = classOf[GetAllNucleicAcidTestPointMessage], name = "GetAllNucleicAcidTestPointMessage"),
    new JsonSubTypes.Type(value = classOf[GetNucleicAcidTestResultsMessage], name = "GetNucleicAcidTestResultsMessage"),
    new JsonSubTypes.Type(value = classOf[QueryTestPointWaitingPersonMessage], name = "QueryTestPointWaitingPersonMessage"),
  )
)
abstract class TSMSPMessage extends JacksonSerializable {
  def handle(): TSMSPReply = reaction() match {
    case Success(value) => value
    case Failure(exception) => TSMSPReply(HandleStatus.ERROR, exception.getMessage)
  }
  def reaction(): Try[TSMSPReply] = Try(TSMSPReply(HandleStatus.OK, "无法识别的消息"))
}
