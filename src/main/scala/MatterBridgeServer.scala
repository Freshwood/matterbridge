import java.util.concurrent.TimeUnit

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.client.rss.RssIntegration
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.{
  NineGagResolveCommand,
  RssReaderActorModel
}
import com.freshsoft.matterbridge.routing.MatterBridgeRoute
import com.freshsoft.matterbridge.server.{MatterBridgeContext, MatterBridgeWebService}
import com.freshsoft.matterbridge.util.MatterBridgeServerConfig

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The matter bridge server entry point which is working as web server
  */
object MatterBridgeServer
    extends App
    with MatterBridgeServerConfig
    with MatterBridgeContext
    with MatterBridgeWebService {

  implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)

  val log = Logging.getLogger(system, this)

  val matterBridgeRoutes = routes ~ new MatterBridgeRoute().routes

  val binding = Http().bindAndHandle(matterBridgeRoutes, host, port) map { binding =>
    log.info(s"REST interface bound to ${binding.localAddress}")
  }

  binding.onFailure {
    case ex: Exception =>
      log.error(ex, "Failed to bind to {}:{}!", host, port)
  }

  system.scheduler.schedule(0 milliseconds,
                            15 seconds,
                            NineGagIntegration.nineGagResolver,
                            NineGagResolveCommand(NineGagIntegration.nineGagWorker))

  system.scheduler
    .schedule(0 milliseconds, 15 minutes, RssIntegration.rssReaderActor, RssReaderActorModel.Start)
}
