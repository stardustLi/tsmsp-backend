package Process

import Utils.DBUtils
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger
//1
object Server {
  val LOGGER = Logger("MainServer")

  def main(args: Array[String]): Unit = try {
    DBUtils.initDatabase()
    implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty[Nothing], "template-system")
    TSMSPPortalHttpServer.startHttpServer(new Routes().routes, system)
  } catch {
    case exception: Exception =>
      LOGGER.error(exception.getMessage)
  }
}
