package Exceptions

case class TokenNotExistsException() extends Exception {
  override def getMessage: String = "错误！用户不存在或登录信息已过期！"
}

case class UserNotExistsException() extends Exception {
  override def getMessage: String = "错误！用户不存在！"
}

case class WrongPasswordException() extends Exception {
  override def getMessage: String = "错误！用户名密码错误或用户不存在"
}

case class UserNameAlreadyExistsException() extends Exception {
  override def getMessage: String = "错误！用户名已经存在了"
}
