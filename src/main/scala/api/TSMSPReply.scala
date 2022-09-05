package api

import models.types.JacksonSerializable

case class TSMSPReply(status: Int, message: Any) extends JacksonSerializable
