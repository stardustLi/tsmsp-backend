package exceptions

import models.fields.IDCard

case class TokenNotExists() extends Exception {
  override def getMessage: String = "错误！用户不存在或登录信息已过期！"
}

case class IDCardNotExists(idCard: IDCard) extends Exception {
  override def getMessage: String = s"错误！不存在身份证号为 ${idCard} 的用户！"
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

case class NoAccessOfIdCard(idCard: IDCard) extends Exception {
  override def getMessage: String = s"错误！无权限访问身份证号为 ${idCard} 的用户！"
}

case class NoPermission() extends Exception {
  override def getMessage: String = "错误！没有权限进行此操作"
}
