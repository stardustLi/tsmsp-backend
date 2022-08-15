package Utils

import Globals.GlobalVariables
import Tables.{UserTable, UserTokenTable, UserTraceTable}
import com.typesafe.config.{Config, ConfigFactory}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object DBUtils {

  lazy val DBConfig: Config = ConfigFactory
    .parseString(s"""""")withFallback(ConfigFactory.load())

  lazy val db=Database.forConfig("tsmsp", config=DBConfig)

  def exec[T] : DBIO[T] => T = action => Await.result(db.run(action), Duration.Inf)
  def initDatabase():Unit={
    exec(
      DBIO.seq(
        sql"CREATE SCHEMA IF NOT EXISTS #${GlobalVariables.mainSchema.get}".as[Long],
        UserTable.userTable.schema.createIfNotExists,
        UserTokenTable.userTokenTable.schema.createIfNotExists,
        UserTraceTable.userTraceTable.schema.createIfNotExists,
      )
    )
  }
  def dropDatabases():Unit={
    exec(
      DBIO.seq(
        UserTable.userTable.schema.dropIfExists,
        UserTokenTable.userTokenTable.schema.dropIfExists,
        UserTraceTable.userTraceTable.schema.dropIfExists,
        sql"DROP SCHEMA IF EXISTS #${GlobalVariables.mainSchema.get}".as[Long],
      )
    )
  }
}
