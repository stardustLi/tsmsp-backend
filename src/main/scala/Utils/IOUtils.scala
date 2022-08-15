package Utils

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.File
import scala.reflect.ClassTag
import scala.swing.Dialog
import scala.util.Try

object IOUtils {

  /** Jackson使用的object mapper */
  val objectMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  /** 序列化 */
  def serialize(o: Any): Try[String] = Try {
    o match {
      case Some(o2) =>
        objectMapper.writeValueAsString(o2)
      case _ =>
        objectMapper.writeValueAsString(o)
    }
  }

  def serializeList[T](o: List[T]): Try[String] = Try {
    o.map(IOUtils.serialize(_).get).mkString("[", ",", "]")
  }


  /** 逆序列化 */
  def deserialize[T](bytes: String)(implicit tag: ClassTag[T]): Try[T] = Try {
    objectMapper.readValue(bytes.getBytes(), tag.runtimeClass).asInstanceOf[T]
  }

  def toObject[T: ClassTag](inputString: Option[String]): Try[Option[T]] = Try {
    inputString.map(IOUtils.deserialize[T](_).get)
  }

  /** 删除文件夹 */
  def recursiveDelete(curFile: File): Unit = {
    if (curFile.isDirectory)
      curFile.listFiles().foreach(recursiveDelete)
    curFile.delete()
  }

  /** 强制删除文件 */
  def deleteFile(fileName: String): Boolean = {
    val file = new File(fileName)
    if (file.exists())
      try {
        var deleteResult: Boolean = false
        var tryCount = 0
        while (!deleteResult && tryCount < 10) {
          System.gc()
          deleteResult = file.delete()
          tryCount += 1
        }
        if (deleteResult)
          return true
        else Dialog.showMessage(null, "错误：删除文件失败！")
      } catch {
        case e: Exception =>
          Dialog.showMessage(null, "错误：" + e.printStackTrace())
      }
    false
  }

  /** 看看folder是否存在 */
  def checkFolder(folderName: String): Unit = {
    val path = new File(folderName)
    if (!path.exists()) path.mkdirs()
  }

  def fromObject(success: Boolean, reply: Object): HttpResponse = HttpResponse(
    status = {
      if (success) StatusCodes.OK else StatusCodes.BadRequest
    },
    entity = IOUtils.serialize(reply).get
  )
  def fromString(success: Boolean, reply: String): HttpResponse = HttpResponse(
    status = {
      if (success) StatusCodes.OK else StatusCodes.BadRequest
    },
    entity = reply
  ).addHeader(RawHeader("Access-Control-Allow-Origin", "*"))

}
