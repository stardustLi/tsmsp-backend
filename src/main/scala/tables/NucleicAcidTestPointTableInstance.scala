package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.{DetailedTrace, NucleicAcidTestPoint}
import utils.db.await

class NucleicAcidTestPointTable(tag: Tag) extends Table[NucleicAcidTestPoint](tag, mainSchema, "nucleic_acid_test_point") {
  import models.types.CustomColumnTypes._
  def place          = column[DetailedTrace]("place")
  def waitingPerson  = column[Int]("waiting_person")
  def *              = (place, waitingPerson).mapTo[NucleicAcidTestPoint]
}

object NucleicAcidTestPointTableInstance {
  val instance = TableQuery[NucleicAcidTestPointTable]
  await(instance.schema.createIfNotExists)

  def filterByPlace(place: DetailedTrace): Query[NucleicAcidTestPointTable, NucleicAcidTestPoint, Seq] = {
    import models.types.CustomColumnTypes._
    instance.filter(point => point.place === place)
  }
}