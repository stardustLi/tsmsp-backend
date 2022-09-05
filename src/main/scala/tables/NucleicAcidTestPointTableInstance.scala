package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.fields.{NucleicAcidTestPointName, TraceID}
import models.NucleicAcidTestPoint
import utils.db.await

class NucleicAcidTestPointTable(tag: Tag) extends Table[NucleicAcidTestPoint](tag, mainSchema, "nucleic_acid_test_point") {
  def place         = column[TraceID]("place")
  def name          = column[NucleicAcidTestPointName]("name", O.PrimaryKey)
  def *             = (place, name).mapTo[NucleicAcidTestPoint]
}

object NucleicAcidTestPointTableInstance {
  val instance: TableQuery[NucleicAcidTestPointTable] = TableQuery[NucleicAcidTestPointTable]
  await(instance.schema.createIfNotExists)

  def filterByName(name: NucleicAcidTestPointName): Query[NucleicAcidTestPointTable, NucleicAcidTestPoint, Seq] =
    instance.filter(point => point.name === name)

  def filterByPlace(place: TraceID): Query[NucleicAcidTestPointTable, NucleicAcidTestPoint, Seq] =
    instance.filter(point => point.place === place)
}
