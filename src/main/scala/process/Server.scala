package process

import utils.db
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

object Server {
  val LOGGER = Logger("MainServer")

  def main(args: Array[String]): Unit = try {
    db.init()
    implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty[Nothing], "template-system")
    TSMSPPortalHttpServer.startHttpServer(new Routes().routes, system)
  } catch {
    case exception: Exception =>
      LOGGER.error(exception.getMessage)
  }
}
