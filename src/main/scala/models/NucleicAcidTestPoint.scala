package models

import models.DetailedTrace

case class NucleicAcidTestPoint(
  place: DetailedTrace,
  waitingPerson: Int
)
