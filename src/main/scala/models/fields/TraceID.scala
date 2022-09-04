package models.fields

import slick.lifted.MappedTo

case class TraceID(value: Int) extends AnyVal with MappedTo[Int]
