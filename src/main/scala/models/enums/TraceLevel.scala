package models.enums

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import slick.lifted.MappedTo

@JsonSerialize(using = classOf[CodeColorSerializer])
@JsonDeserialize(using = classOf[CodeColorSerializerDeserializer])
sealed abstract class TraceLevel(val value: Int) extends MappedTo[Int]

object TraceLevel {
  case object PROVINCE extends TraceLevel(0)
  case object CITY extends TraceLevel(1)
  case object COUNTY extends TraceLevel(2)
  case object STREET extends TraceLevel(3)

  def objectList: List[TraceLevel] = List(PROVINCE, CITY, COUNTY, STREET)
  def getType(value: Int): TraceLevel = objectList.filter(level => level.value == value).head
}

class TraceLevelSerializer extends StdSerializer[TraceLevel](classOf[TraceLevel]) {
  override def serialize(traceLevel: TraceLevel, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeNumber(traceLevel.value)
}

class TraceLevelSerializerDeserializer extends StdDeserializer[TraceLevel](classOf[TraceLevel]) {
  override def deserialize(p: JsonParser, ctx: DeserializationContext): TraceLevel =
    TraceLevel.getType(p.getIntValue)
}
