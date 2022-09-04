package utils

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.ClassTag
import scala.util.Try

object io {
  /* Jackson 使用的 object mapper */
  val objectMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  /* 序列化 */
  def serialize(entry: Any): Try[String] = Try {
    entry match {
      case Some(element) =>
        objectMapper.writeValueAsString(element)
      case _ =>
        objectMapper.writeValueAsString(entry)
    }
  }

  /* 逆序列化 */
  def deserialize[T](bytes: String)(implicit tag: ClassTag[T]): Try[T] = Try {
    objectMapper.readValue(bytes.getBytes(), tag.runtimeClass).asInstanceOf[T]
  }

  def fromObject(success: Boolean, reply: Object): HttpResponse = HttpResponse(
    status = {
      if (success) StatusCodes.OK else StatusCodes.BadRequest
    },
    entity = io.serialize(reply).get
  )

  def fromString(success: Boolean, reply: String): HttpResponse = HttpResponse(
    status = {
      if (success) StatusCodes.OK else StatusCodes.BadRequest
    },
    entity = reply
  ).addHeader(RawHeader("Access-Control-Allow-Origin", "*"))
}
