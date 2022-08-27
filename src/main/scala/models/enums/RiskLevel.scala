package models.enums

import slick.lifted.MappedTo

sealed abstract class RiskLevel(val value: Int) extends MappedTo[Int]
case object LOW extends RiskLevel(0)
case object MEDIUM extends RiskLevel(1)
case object HIGH extends RiskLevel(2)

object RiskLevel {
  def objectList: List[RiskLevel] = List(LOW, MEDIUM, HIGH)
  def getType(value: Int): RiskLevel = objectList.filter(level => level.value == value).head
}
