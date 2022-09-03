package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.fields.NucleicAcidTestPointName
import models.{DetailedTrace, NucleicAcidTestPoint}
import utils.db.await

class NucleicAcidTestPointTable(tag: Tag) extends Table[NucleicAcidTestPoint](tag, mainSchema, "nucleic_acid_test_point") {
  import models.types.CustomColumnTypes._
  def place         = column[DetailedTrace]("place")
  def name          = column[NucleicAcidTestPointName]("name", O.PrimaryKey)
  def *             = (place, name).mapTo[NucleicAcidTestPoint]
}

object NucleicAcidTestPointTableInstance {
  val instance = TableQuery[NucleicAcidTestPointTable]
  await(instance.schema.createIfNotExists)

  def filterByName(name: NucleicAcidTestPointName): Query[NucleicAcidTestPointTable, NucleicAcidTestPoint, Seq] =
    instance.filter(point => point.name === name)
}
