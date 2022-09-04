package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import scala.util.Try

import globals.GlobalVariables.mainSchema
import models.fields.{IDCard, NucleicAcidTestPointName}
import models.NucleicAcidTestAppoint
import utils.db.await

class NucleicAcidTestAppointTable(tag: Tag) extends Table[NucleicAcidTestAppoint](tag, mainSchema, "nucleic_acid_test_appoint") {
  import models.types.CustomColumnTypes._
  def idCard      = column[IDCard]("id_card")
  def testPlace   = column[NucleicAcidTestPointName]("test_place")
  def appointTime = column[Long]("appoint_time")
  def pK          = primaryKey("nucleic_acid_test_appoint_pk", (idCard, testPlace))
  def *           = (idCard, testPlace, appointTime).mapTo[NucleicAcidTestAppoint]
}

object NucleicAcidTestAppointTableInstance {
  val instance: TableQuery[NucleicAcidTestAppointTable] = TableQuery[NucleicAcidTestAppointTable]
  Try(await(instance.schema.createIfNotExists))

  def filterByIDCard(idCard: IDCard): Query[NucleicAcidTestAppointTable, NucleicAcidTestAppoint, Seq] =
    instance.filter(appoint => appoint.idCard === idCard)

  def filterByPlace(testPlace: NucleicAcidTestPointName): Query[NucleicAcidTestAppointTable, NucleicAcidTestAppoint, Seq] =
    instance.filter(appoint => appoint.testPlace === testPlace)
}
