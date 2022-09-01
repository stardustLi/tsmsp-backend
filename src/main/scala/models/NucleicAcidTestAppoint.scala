package models

import models.fields.IDCard

case class NucleicAcidTestAppoint(
  idCard: IDCard,
  appointTime: Long,
  testPlace: DetailedTrace
)
