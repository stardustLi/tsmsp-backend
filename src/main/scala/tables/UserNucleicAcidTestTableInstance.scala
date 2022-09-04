package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.UserNucleicAcidTest
import models.fields.{IDCard, NucleicAcidTestPointName}
import utils.db.await

class UserNucleicAcidTestTable(tag: Tag) extends Table[UserNucleicAcidTest](tag, mainSchema, "user_nucleic_acid_test") {
  def idCard    = column[IDCard]("id_card")
  def testPlace = column[NucleicAcidTestPointName]("test_place")
  def time      = column[Long]("time")
  def result    = column[Boolean]("result")
  def *         = (idCard, testPlace, time, result).mapTo[UserNucleicAcidTest]
}

object UserNucleicAcidTestTableInstance {
  val instance: TableQuery[UserNucleicAcidTestTable] = TableQuery[UserNucleicAcidTestTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Query[UserNucleicAcidTestTable, UserNucleicAcidTest, Seq] =
    instance.filter(test => test.idCard === idCard.toLowerCase())

  def filterByResult(result: Boolean): Query[UserNucleicAcidTestTable, UserNucleicAcidTest,Seq] =
    instance.filter(test => test.result === result)
}
