package exceptions

import models.fields.{IDCard, NucleicAcidTestPointName, UserName}

case class TokenNotExists() extends Exception {
  override def getMessage: String = "错误！用户不存在或登录信息已过期！"
}

case class UserNameInvalid(userName: UserName) extends Exception {
  override def getMessage: String = s"用户名 ${userName.value} 不合法！"
}

case class PasswordInvalid() extends Exception {
  override def getMessage: String = s"密码太弱或包含非法字符！"
}

case class IDCardInvalid(idCard: IDCard) extends Exception {
  override def getMessage: String = s"身份证号 ${idCard.value} 不合法！"
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
  override def getMessage: String = s"错误！无权限访问 (或不存在) 身份证号为 ${idCard.value} 的用户！"
}

case class NoPermission() extends Exception {
  override def getMessage: String = "错误！没有权限进行此操作"
}

case class NucleicAcidTestPointNameInvalid(name: NucleicAcidTestPointName) extends Exception {
  override def getMessage: String = s"核酸测试点名称 ${name.value} 不合法！"
}

case class NucleicAcidTestPointNotExists(name: NucleicAcidTestPointName) extends Exception {
  override def getMessage: String = s"核酸测试点 ${name.value} 不存在！"
}

case class AppointAlreadyExists(idCard: IDCard) extends Exception {
  override def getMessage: String = s"错误！身份证号为 ${idCard.value} 的核酸预约已存在"
}

case class NoAppoint(idCard: IDCard) extends Exception {
  override def getMessage: String = s"错误！身份证号为 ${idCard.value} 的用户未进行预约"
}
