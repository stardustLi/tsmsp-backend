package models.fields

import slick.lifted.MappedTo

case class NucleicAcidTestPointName(value: String) extends AnyVal with MappedTo[String] {
  def isValid(): Boolean =
    value.nonEmpty && value.forall(ch =>
      ('A' <= ch && ch <= 'Z') ||
      ('a' <= ch && ch <= 'z') ||
      ('0' <= ch && ch <= '9') ||
      ('\u4e00' <= ch && ch <= '\u9fa5') ||
      (ch == '-') ||
      (ch == '_')
    )
}
