package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try
import globals.GlobalVariables.mainSchema
import models.{DetailedTrace, NucleicAcidTestPoint}
import utils.db.await

class NucleicAcidTestPointTable(tag: Tag) extends Table[NucleicAcidTestPoint](tag, mainSchema, "nucleic_acid_test_point") {
  import models.CustomColumnTypes._
  def place          = column[DetailedTrace]("place")
  def waitingPerson  = column[Int]("waiting_person")
  def *              = (place, waitingPerson).mapTo[NucleicAcidTestPoint]
}

object NucleicAcidTestPointTableInstance {
  val instance = TableQuery[NucleicAcidTestPointTable]
  await(instance.schema.createIfNotExists)

  def filterByPlace(place: DetailedTrace): Query[NucleicAcidTestPointTable, NucleicAcidTestPoint, Seq] = {
    import models.CustomColumnTypes._
    instance.filter(nucleic_acid_test_point => nucleic_acid_test_point.place === place)
  }
}