package Utils

import org.joda.time.DateTime

object StringUtils {
  /** 定义win/linux下面的slash */
  val slash: String = if (System.getProperty("os.name").startsWith("Windows")) "\\" else "/"

  /** 中文字符变成数字 */
  def chineseToNumber(ch: String): Double = {
    ch match {
      case "一" => 1
      case "二" => 2
      case "三" => 3
      case "四" => 4
      case "五" => 5
      case "六" => 6
      case "七" => 7
      case "八" => 8
      case "九" => 9
      case "半" => 0.5
      case "十" => 10
      case "百" => 100
      case "千" => 1000
      case _ => 0 //meaning not defined.
    }
  }
  /** 产生一个length长度的随机数字串 */
  def randomNumber(length: Int): String = {
    val chars = ('0' to '9')
    val sb = new StringBuilder
    for (_ <- 1 to length) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }

  /** 产生一个length长度的随机字符串 */
  def randomString(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z')
    val sb = new StringBuilder
    for (_ <- 1 to length) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }


  def levenshteinDistance(str1: String, str2: String): Int = {
    val xLen = str1.length
    val yLen = str2.length
    val dp = Array.fill(xLen + 1)(Array.fill(yLen + 1)(0))
    for (i <- Range(0, xLen + 1))
      for (j <- Range(0, yLen + 1))
        dp(i)(j) = i + j
    for (i <- Range(1, xLen + 1))
      for (j <- Range(1, yLen + 1)) {
        var d = 0
        if (str1(i - 1) == str2(j - 1))
          d = 0
        else
          d = 1
        dp(i)(j) = List(dp(i - 1)(j) + 1, dp(i)(j - 1) + 1, dp(i - 1)(j - 1) + d).min
      }
    dp(xLen)(yLen)
  }

  def toDate(time: DateTime): String = time.toString("yyyy-MM-dd")

  def toDate(time: Long): String = new DateTime(time).toString("yyyy-MM-dd")

  def toDate(time: String): String = new DateTime(time).toString("yyyy-MM-dd")

}
