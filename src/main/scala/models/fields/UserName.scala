package models.fields

import slick.lifted.MappedTo

case class UserName(value: String) extends AnyVal with MappedTo[String] {
  def isValid(): Boolean =
    value.nonEmpty && value.length <= 20 && value.forall(ch =>
      ('A' <= ch && ch <= 'Z') ||
      ('a' <= ch && ch <= 'z') ||
      ('0' <= ch && ch <= '9') ||
      (ch == '-') ||
      (ch == '_')
    )
}
