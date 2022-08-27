package utils

import globals.GlobalVariables.mainSchema
import com.typesafe.config.{Config, ConfigFactory}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object db {
  lazy val DBConfig: Config = ConfigFactory.parseString("").withFallback(ConfigFactory.load())
  lazy val db: Database = Database.forConfig("tsmsp", config = DBConfig)

  def await[T]: DBIO[T] => T = action => Await.result(db.run(action), Duration.Inf)

  def init(): Unit = await(sql"CREATE SCHEMA IF NOT EXISTS #${mainSchema.get}".as[Long])
}
