package models.enums

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import slick.lifted.MappedTo

@JsonSerialize(using = classOf[RiskLevelSerializer])
@JsonDeserialize(using = classOf[RiskLevelSerializerDeserializer])
sealed abstract class RiskLevel(val value: Int) extends MappedTo[Int]
case object LOW extends RiskLevel(0)
case object MEDIUM extends RiskLevel(1)
case object HIGH extends RiskLevel(2)

object RiskLevel {
  def objectList: List[RiskLevel] = List(LOW, MEDIUM, HIGH)
  def getType(value: Int): RiskLevel = objectList.filter(level => level.value == value).head
}

class RiskLevelSerializer extends StdSerializer[RiskLevel](classOf[RiskLevel]) {
  override def serialize(riskLevel: RiskLevel, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeNumber(riskLevel.value)
}

class RiskLevelSerializerDeserializer extends StdDeserializer[RiskLevel](classOf[RiskLevel]) {
  override def deserialize(p: JsonParser, ctx: DeserializationContext): RiskLevel =
    RiskLevel.getType(p.getIntValue)
}
