package exceptions

case class TokenNotExists() extends Exception {
  override def getMessage: String = "错误！用户不存在或登录信息已过期！"
}

case class UserNotExists() extends Exception {
  override def getMessage: String = "错误！用户不存在！"
}

case class WrongPassword() extends Exception {
  override def getMessage: String = "错误！用户名密码错误或用户不存在"
}

case class UserNameAlreadyExists() extends Exception {
  override def getMessage: String = "错误！用户名已经存在了"
}
