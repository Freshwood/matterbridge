import java.util.concurrent.TimeUnit

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.util.Timeout
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.NineGagResolveCommand
import com.freshsoft.matterbridge.routing.MatterBridgeRoute
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.MatterBridgeServerConfig

import scala.concurrent.duration._
import scala.language.postfixOps

/**
	* The matter bridge server entry point which is working as web server
	*/
object MatterBridgeServer extends App
	with MatterBridgeServerConfig
	with WithActorContext {

	implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)

	val log = Logging.getLogger(system, this)

	val matterBridgeRoutes = new MatterBridgeRoute

	val binding = Http().bindAndHandle(matterBridgeRoutes.routes, host, port) map { binding =>
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
}
