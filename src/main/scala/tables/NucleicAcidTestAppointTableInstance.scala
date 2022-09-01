package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import globals.GlobalVariables.mainSchema
import models.fields.IDCard
import models.{DetailedTrace, NucleicAcidTestAppoint}
import utils.db.await

class NucleicAcidTestAppointTable(tag: Tag) extends Table[NucleicAcidTestAppoint](tag, mainSchema, "nucleic_acid_test_appoint") {
  import models.types.CustomColumnTypes._
  def idCard      = column[IDCard]("id_card")
  def appointTime = column[Long]("appoint_time")
  def place       = column[DetailedTrace]("place")
  def *           = (idCard, appointTime, place).mapTo[NucleicAcidTestAppoint]
}

object NucleicAcidTestAppointTableInstance {
  val instance = TableQuery[NucleicAcidTestAppointTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Query[NucleicAcidTestAppointTable, NucleicAcidTestAppoint, Seq] = {
    instance.filter(nucleic_acid_test_appoint => nucleic_acid_test_appoint.idCard === idCard)
  }

  def filterByPlace(place: DetailedTrace): Query[NucleicAcidTestAppointTable, NucleicAcidTestAppoint, Seq] = {
    import models.types.CustomColumnTypes._
    instance.filter(nucleic_acid_test_appoint => nucleic_acid_test_appoint.place === place)
  }//
}