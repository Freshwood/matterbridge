package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration.NineGagWorkerCommand
import com.freshsoft.matterbridge.util.MatterBridgeHttpClient
import model.MatterBridgeEntities.NineGagGifResult
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.{ExecutionContext, Future}

/**
	* The nine gag worker which is resolving the urls
	*/
class NineGagWorker(nineGagReceiver: ActorRef) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case command: NineGagWorkerCommand =>
      retrieve9GagGifs(command) foreach { result =>
        log.info(s"Found ${result.size} gifs from $command")

        if (result.isEmpty) {
          log.info(s"No gifs found from url ${command.nineGagUrl}")
        } else {
          result foreach (nineGagReceiver ! _)
        }
      }
  }

  /**
		* Get concurrent the List of gifs from NineGag
		*
		* @param command The url to retrieve the gifs
		* @return A Future list of NineGagGifResult
		*/
  private def retrieve9GagGifs(command: NineGagWorkerCommand): Future[List[NineGagGifResult]] =
    MatterBridgeHttpClient.getUrlContent(command.nineGagUrl) map {
      case x if x.isEmpty  => log.info(s"Get no content from ${command.nineGagUrl}"); Nil
      case x if !x.isEmpty => resolveGifsFromContent(x, command.relatedCategory)
    }

  /**
		* Get the gifs from the content
		*
		* @param htmlContent The web result to retrieve the information
		* @return A list of NineGagGifResult
		*/
  private def resolveGifsFromContent(htmlContent: String, category: String) = {
    val browser = JsoupBrowser()
    val doc = browser.parseString(htmlContent)

    val articles = doc >> elements("article")

    val gifResults = for {
      article <- articles
      headline <- article("header h2") map (_ >> allText("a"))
      gifSrc <- (article >> elementList("div a div") >?> attr("data-image")).flatten
    } yield (headline, gifSrc)

    gifResults.map(r => NineGagGifResult(r._1, r._2, category)).toList
  }
}
