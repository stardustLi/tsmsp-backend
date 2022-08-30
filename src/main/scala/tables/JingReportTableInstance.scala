package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import globals.GlobalVariables.mainSchema
import models.JingReport
import models.fields.IDCard
import utils.db.await

class JingReportTable(tag: Tag) extends Table[JingReport](tag, mainSchema, "jing_report") {
  def idCard = column[IDCard]("id_card", O.PrimaryKey)
  def reason = column[String]("reason")
  def time   = column[Long]("time")
  def *      = (idCard, reason, time).mapTo[JingReport]
}

object JingReportTableInstance {
  val instance = TableQuery[JingReportTable]
  await(instance.schema.createIfNotExists)

  def filterByIdCard(idCard: IDCard): Query[JingReportTable, JingReport, Seq] =
    instance.filter(Appeal => Appeal.idCard === idCard)
}
