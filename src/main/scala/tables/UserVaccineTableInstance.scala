package tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try
import globals.GlobalVariables.mainSchema
import models.UserVaccine
import models.fields.IDCard
import utils.db.await

class UserVaccineTable(tag: Tag) extends Table[UserVaccine](tag, mainSchema, "user_vaccine") {
  def idCard      = column[IDCard]("id_card")
  def manufacture = column[String]("manufacture")
  def time        = column[Long]("time")
  def vaccineType = column[Int]("number")
  def *           = (idCard, manufacture, time, vaccineType).mapTo[UserVaccine]
}

object UserVaccineTableInstance {
  val instance = TableQuery[UserVaccineTable]
  await(instance.schema.createIfNotExists)

  def filterByIDCard(idCard: IDCard): Query[UserVaccineTable, UserVaccine, Seq] =
    instance.filter(user_vaccine => user_vaccine.idCard === idCard)
}