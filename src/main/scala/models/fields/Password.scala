package models.fields

import slick.lifted.MappedTo

case class Password(value: String) extends AnyVal with MappedTo[String] {
  def isValid(): Boolean = value.nonEmpty && value.length >= 6
}
