package tables

import slick.jdbc.PostgresProfile.ReturningInsertActionComposer
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import globals.GlobalVariables.mainSchema
import models.enums.TraceLevel
import models.TraceTree
import models.fields.TraceID
import utils.db.await

class TraceTreeTable(tag: Tag) extends Table[TraceTree](tag, mainSchema, "trace_tree") {
  import models.types.CustomColumnTypes._
  def id       = column[TraceID]("id", O.PrimaryKey, O.AutoInc)
  def name     = column[String]("name")
  def level    = column[TraceLevel]("level")
  def parentID = column[TraceID]("parent_id")
  def *        = (id, name, level, parentID).mapTo[TraceTree]
}

object TraceTreeTableInstance {
  val instance: TableQuery[TraceTreeTable] = TableQuery[TraceTreeTable]
  lazy val instanceWithId: ReturningInsertActionComposer[TraceTree, TraceID] = instance.returning(instance.map(trace => trace.id))
  await(instance.schema.createIfNotExists)

  def filterByID(id: TraceID): Query[TraceTreeTable, TraceTree, Seq] =
    instance.filter(trace => trace.id === id)

  def filterByPID(parentID: TraceID): Query[TraceTreeTable, TraceTree, Seq] =
    instance.filter(trace => trace.parentID === parentID)

  def filterByNameLevelPID(name: String, level: TraceLevel, parentID: TraceID): Query[TraceTreeTable, TraceTree, Seq] = {
    import models.types.CustomColumnTypes._
    instance.filter(trace => trace.name === name && trace.level === level && trace.parentID === parentID)
  }
}
