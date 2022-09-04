package utils

import scala.collection.mutable
import util.Random

object string {
  def randomString(length: Int, charset: Seq[Char]): String = {
    val builder: mutable.StringBuilder = new mutable.StringBuilder()
    (1 to length).foldLeft(builder)((builder, _) => {
      builder.append(charset(Random.nextInt(charset.length)))
    }).toString
  }

  def randomToken(length: Int): String = randomString(length, ('a' to 'z') ++ ('A' to 'Z'))
}
