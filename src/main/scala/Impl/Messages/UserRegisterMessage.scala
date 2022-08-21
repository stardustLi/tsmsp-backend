package Impl.Messages
import Exceptions.UserNameAlreadyExistsException
import Impl.{STATUS_OK, TSMSPReply}
import Tables.{UserTable, UserTokenTable}
import Utils.DBUtils
import org.joda.time.DateTime

import scala.util.Try

case class UserRegisterMessage(userName : String, password : String, realName : String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    if (UserTable.checkUserExists(userName).get) throw UserNameAlreadyExistsException()
    else {
      DBUtils.exec(UserTable.addUser(userName, password, realName).get.andThen(UserTokenTable.addRow(userName).get))
      TSMSPReply(STATUS_OK,  UserTokenTable.checkToken(userName).get)
    }
  }
}
