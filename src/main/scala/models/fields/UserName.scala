package models.fields

import slick.lifted.MappedTo

case class UserName(value: String) extends AnyVal with MappedTo[String]
