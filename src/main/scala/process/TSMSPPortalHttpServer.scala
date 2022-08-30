package process

import globals.GlobalVariables
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Sink
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success}

object TSMSPPortalHttpServer {
  val LOGGER: Logger = Logger("HttpServer")

  /* 搭建 http 服务器，监听相应端口 */
  def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic

    val settings: CorsSettings.Default = CorsSettings.defaultSettings.copy(
      allowedOrigins = HttpOriginRange.* // * refers to all
    )

    val futureBinding =
      Http().newServerAt(
        GlobalVariables.listenAddress,
        GlobalVariables.listenPortal
      ).connectionSource().to(Sink.foreach {
        connection => {
          val remoteIP = connection.remoteAddress.getAddress.toString.replaceAll("/", "")
          val health = path("health") & cors(settings)
          LOGGER.info("Accepted connection from " + remoteIP)
          connection.handleWith(
            concat(
              routes,
              health(complete("OK!"))
            )
          )
        }
      }).run()

    import system.executionContext
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        LOGGER.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        LOGGER.error(s"Failed to bind HTTP endpoint, terminating system ${ex.getMessage}")
        system.terminate()
    }
  }
}
