package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.enums.TraceLevel
import models.TraceTree
import utils.db.await

class TraceTreeTable(tag: Tag) extends Table[TraceTree](tag, mainSchema, "trace_tree") {
  import models.types.CustomColumnTypes._
  def id       = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name     = column[String]("name")
  def level    = column[TraceLevel]("level")
  def parentId = column[Int]("parent_id")
  def *        = (id, name, level, parentId).mapTo[TraceTree]
}

object TraceTreeTableInstance {
  val instance = TableQuery[TraceTreeTable]
  await(instance.schema.createIfNotExists)

  def filterByID(id: Int): Query[TraceTreeTable, TraceTree, Seq] =
    instance.filter(trace => trace.id === id)
}
