package process

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.Logger
import java.net.InetSocketAddress
import scala.concurrent.Future
import scala.util.{Failure, Success}

import globals.GlobalVariables

object TSMSPPortalHttpServer {
  val LOGGER: Logger = Logger("HttpServer")

  /* 搭建 http 服务器，监听相应端口 */
  def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic

    val futureBinding: Future[Http.ServerBinding] =
      Http().newServerAt(
        GlobalVariables.listenAddress,
        GlobalVariables.listenPortal
      ).connectionSource().to(Sink.foreach {
        connection => {
          val remoteIP: String = connection.remoteAddress.getAddress.toString.replaceAll("/", "")
          LOGGER.info("Accepted connection from " + remoteIP)
          connection.handleWith(routes)
        }
      }).run()

    import system.executionContext
    futureBinding.onComplete {
      case Success(binding) =>
        val address: InetSocketAddress = binding.localAddress
        LOGGER.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        LOGGER.error(s"Failed to bind HTTP endpoint, terminating system ${ex.getMessage}")
        system.terminate()
    }
  }
}
