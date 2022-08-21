package Process

import Impl.Messages.TSMSPMessage
import Utils.IOUtils.{fromObject, fromString}
import Process.Server.logger
import Utils.IOUtils
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime

import scala.util.{Failure, Try}

/** http不同的路径用于处理不同的通信 */
import scala.util.Success
class Routes()(implicit val system: ActorSystem[_]) {
  val settings: CorsSettings.Default = CorsSettings.defaultSettings.copy(
    allowedOrigins = HttpOriginRange.* // * refers to all
  )
  val routes: Route = {
      concat(
        (path("api") & cors(settings)) {
          post {
            entity(as[String]) { bytes =>
              Logger("TSMSP-Portal-Route").info("$ api got a post: " + bytes)
              Try {
                val message = IOUtils.deserialize[TSMSPMessage](bytes).get
                message.handle()
              } match {
                case Success(value) =>
                  logger.info("处理成功")
                  complete(fromObject(success = true, value))
                case Failure(e: Throwable) =>
                  logger.error(s"出现未知错误${e.getMessage}")
                  complete(fromString(success = true, e.getMessage))
              }
            }
          }
        },
      )
  }
}
