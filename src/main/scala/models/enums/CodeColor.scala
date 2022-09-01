package models.enums

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import slick.lifted.MappedTo

@JsonSerialize(using = classOf[CodeColorSerializer])
@JsonDeserialize(using = classOf[CodeColorSerializerDeserializer])
sealed abstract class CodeColor(val value: Int) extends MappedTo[Int] {
  def next: CodeColor
}

object CodeColor {
  case object GREEN extends CodeColor(0) {
    def next: CodeColor = GREEN
  }

  case object ALERT extends CodeColor(1) {
    def next: CodeColor = GREEN
  }

  case object YELLOW extends CodeColor(2) {
    def next: CodeColor = ALERT
  }

  case object RED extends CodeColor(3) {
    def next: CodeColor = YELLOW
  }

  def objectList: List[CodeColor] = List(GREEN, ALERT, YELLOW, RED)
  def getType(value: Int): CodeColor = objectList.filter(level => level.value == value).head

  def max(color1: CodeColor, color2: CodeColor) = getType(color1.value max color2.value)
}

class CodeColorSerializer extends StdSerializer[CodeColor](classOf[CodeColor]) {
  override def serialize(CodeColor: CodeColor, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeNumber(CodeColor.value)
}

class CodeColorSerializerDeserializer extends StdDeserializer[CodeColor](classOf[CodeColor]) {
  override def deserialize(p: JsonParser, ctx: DeserializationContext): CodeColor =
    CodeColor.getType(p.getIntValue)
}
