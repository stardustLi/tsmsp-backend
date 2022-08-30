package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try
import globals.GlobalVariables.mainSchema
import models.UserNucleicAcidTest
import models.fields.IDCard
import utils.db.await

class UserNucleicAcidTestTable(tag: Tag) extends Table[UserNucleicAcidTest](tag, mainSchema, "user_nucleic_acid_test") {
  def idCard  = column[IDCard]("id_card")
  def time    = column[Long]("time")
  def result  = column[Boolean]("number")
  def *       = (idCard, time, result).mapTo[UserNucleicAcidTest]
}

object UserNucleicAcidTestTableInstance {
  val instance = TableQuery[UserNucleicAcidTestTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Query[UserNucleicAcidTestTable, UserNucleicAcidTest, Seq] =
    instance.filter(user_nucleic_acid_test => user_nucleic_acid_test.idCard === idCard)

  def filterByResult(result: Boolean): Query[UserNucleicAcidTestTable, UserNucleicAcidTest,Seq] =
    instance.filter(user_nucleic_acid_test => user_nucleic_acid_test.result === result)
}