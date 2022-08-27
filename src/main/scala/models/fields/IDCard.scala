package models.fields

import slick.lifted.MappedTo

case class IDCard(value: String) extends AnyVal with MappedTo[String]
