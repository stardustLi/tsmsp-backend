package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.UserColor
import models.enums.CodeColor
import models.fields.IDCard
import utils.db.await

class UserColorTable(tag: Tag) extends Table[UserColor](tag, mainSchema, "user_color") {
  import models.types.CustomColumnTypes._
  def idCard = column[IDCard]("id_card", O.PrimaryKey)
  def color  = column[CodeColor]("code_color")
  def *      = (idCard, color).mapTo[UserColor]
}

object UserColorTableInstance {
  val instance = TableQuery[UserColorTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Query[UserColorTable, UserColor, Seq] =
    instance.filter(user => user.idCard === idCard)
}
