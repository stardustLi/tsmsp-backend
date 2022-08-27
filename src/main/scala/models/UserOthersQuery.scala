package models

import models.fields.{UserName, IDCard}

case class UserOthersQuery (
  userName: UserName,
  idcardOthers: IDCard
)
