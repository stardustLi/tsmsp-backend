package Utils

import org.joda.time.DateTime
import util.Random

object StringUtils {
  /* 定义 win/linux 下面的 slash = path.sep */
  val slash: String = if (System.getProperty("os.name").startsWith("Windows")) "\\" else "/"

  /* 中文字符变成数字 */
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
      case _ => 0 // meaning not defined.
    }
  }

  def randomString(length: Int, charset: Seq[Char]): String = {
    val builder = new StringBuilder()
    (1 to length).foldLeft(builder)((builder, _) => {
      builder.append(charset(Random.nextInt(charset.length)))
    }).toString
  }

  def randomToken(length: Int): String = randomString(length, ('a' to 'z') ++ ('A' to 'Z'))

  def toDate(time: DateTime): String = time.toString("yyyy-MM-dd")

  def toDate(time: Long): String = toDate(new DateTime(time))

  def toDate(time: String): String = toDate(new DateTime(time))
}
