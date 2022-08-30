package models

import models.fields.IDCard

case class UserVaccine(
  idCard: IDCard,
  manufacture: String,
  time: Long, //接种时间
  vaccineType: Int //接种剂次
)
