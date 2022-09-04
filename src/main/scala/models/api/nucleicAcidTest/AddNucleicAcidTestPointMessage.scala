package models.api.nucleicAcidTest

import models.DetailedTrace
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.NucleicAcidTestPointName
import org.joda.time.DateTime
import services.NucleicAcidTestService.addNucleicAcidTestPoint

import scala.util.Try

case class AddNucleicAcidTestPointMessage(userToken: String, place: DetailedTrace, name: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addNucleicAcidTestPoint(userToken, place, name, now).get)
  }
}
