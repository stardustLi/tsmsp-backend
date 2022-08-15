package Impl.Messages
import Exceptions.WrongPasswordException
import Impl.{STATUS_OK, TSMSPReply}
import Tables.{UserTable, UserTokenTable}
import org.joda.time.DateTime

import scala.util.Try

case class UserLoginMessage(userName : String, password : String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    if (UserTable.checkPassword(userName, password).get) TSMSPReply(STATUS_OK, UserTokenTable.checkToken(userName).get)
    else throw WrongPasswordException()
  }
}
