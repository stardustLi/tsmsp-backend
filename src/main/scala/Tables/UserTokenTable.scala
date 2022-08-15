package Tables


import Exceptions.{TokenNotExistsException, UserNotExistsException}
import Globals.GlobalVariables
import Utils.{DBUtils, StringUtils}
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class UserTokenRow(
                          userName : String,
                          token : String,
                          refreshTime : Long
                         )

class UserTokenTable(tag : Tag) extends Table[UserTokenRow](tag, GlobalVariables.mainSchema, "user_token") {
  def userName = column[String]("user_name", O.PrimaryKey)
  def token = column[String]("token")
  def refreshTime = column[Long]("refresh_time")
  def * = (userName, token, refreshTime).mapTo[UserTokenRow]
}

object UserTokenTable {
  val userTokenTable = TableQuery[UserTokenTable]

  def addRow(userName : String) : Try[DBIO[Int]] = Try {
    userTokenTable += UserTokenRow(userName, "", DateTime.now().minusYears(2).getMillis)
  }
  def checkToken(userName : String) : Try[String] = Try {
    val nowTokenPair = DBUtils.exec(userTokenTable.filter(ut => ut.userName === userName).map(ut => (ut.token, ut.refreshTime)).result.headOption).getOrElse(throw UserNotExistsException())
    if (nowTokenPair._2 >= DateTime.now().minusHours(2).getMillis) {
      DBUtils.exec(userTokenTable.filter(ut => ut.userName === userName).map(_.refreshTime).update(DateTime.now().getMillis))
      nowTokenPair._1
    } else {
      var newToken : String = StringUtils.randomString(30)
      while (DBUtils.exec(userTokenTable.filter(_.token === newToken).size.result) > 0) newToken = StringUtils.randomString(30)
      DBUtils.exec(userTokenTable.filter(_.userName === userName).map(ut => (ut.token, ut.refreshTime)).update((newToken, DateTime.now().getMillis)))
      newToken
    }
  }
  def checkUserName(token : String) : Try[String] = Try {
    DBUtils.exec(userTokenTable.filter(ut => ut.token === token && ut.refreshTime >= DateTime.now().minusHours(2).getMillis).map(_.userName).result.headOption).getOrElse(
      throw TokenNotExistsException()
    )
  }
}