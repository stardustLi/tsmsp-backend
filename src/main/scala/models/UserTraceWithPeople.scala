package models

import models.fields.IDCard

case class UserTraceWithPeople (
  ThisPeople: IDCard, // 本人身份证号
  PeopleMetWithThisPeople: IDCard, // 密接者身份证号
  time: Long
)
