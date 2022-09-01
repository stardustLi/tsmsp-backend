package models

import models.fields.{IDCard, UserName}

case class UserTraceWithPeople (
  ThisPeople: IDCard, // 本人身份证号
  CCUserName: UserName, // 密接者用户名
  CCIDCard: IDCard, // 密接者身份证号
  time: Long
)
