package models.fields

import java.lang.Long
import slick.lifted.MappedTo

private object IDCardChecker {
  val checkIndices: String = "1x864209753"
}

case class IDCard(value: String) extends AnyVal with MappedTo[String] {
  def isValid(): Boolean =
    value.length == 18 && {
      val str = value.toLowerCase()
      val prefix = str.substring(0, 17)
      prefix.forall(ch => '0' <= ch && ch <= '9') &&
        IDCardChecker.checkIndices.charAt((Long.parseLong(prefix, 13) % 11).toInt) == str.last
    }

  def toLowerCase(): IDCard = IDCard(value.toLowerCase)
}
