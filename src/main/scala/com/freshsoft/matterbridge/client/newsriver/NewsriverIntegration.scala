package com.freshsoft.matterbridge.client.newsriver

import java.net.URLEncoder

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model._
import akka.util.ByteString
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import com.freshsoft.matterbridge.entity.MatterBridgeEntities._
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.WithConfig
import spray.json._

import scala.concurrent.Future
import scala.language.postfixOps

/**
	* The news river integration object
	*/
object NewsriverIntegration extends IMatterBridgeResult
	with WithConfig
	with WithActorContext
	with ISlashCommandJsonSupport {

	val log = Logging.getLogger(system, this)

	val apiToken = "sBBqsGXiYgF0Db5OV5tAw9cKkG-R9HP8i_Hw0VCYICEOnIvuIlisyP67o0v1pThT"

	val apiHeader = HttpHeader.parse("authorization", apiToken) match {
		case Ok(header, errors) => header
		case _ => throw new IllegalArgumentException("Could not parse api token to header")
	}

	private def buildNewsriverUrl(title: String) = {
		s"https://api.newsriver.io/v2/search?query=title:$title&limit=10"
	}

	/**
		* Get a raw response from the newsriver site
		*
		* @param url the pre builded url to retrieve the data
		* @return A raw json response
		*/
	private def getResponse(url: String) = Http().singleRequest(
		HttpRequest(uri = url, method = HttpMethods.GET, headers = collection.immutable.Seq(apiHeader)))
		.flatMap { case HttpResponse(StatusCodes.OK, headers, entity, _) =>
			entity.dataBytes.runFold(ByteString(""))(_ ++ _).map {
				x => x.decodeString("UTF-8")
			}

		// Just return nothing when the site is down or we don't receive what we want
		// The further handling is above
		case _ => Future {
			""
		}
		}

		private def sendNewsriverResultToIncomingWebhook(incomingResponse: IncomingResponse) =
		Http().singleRequest(HttpRequest(uri = newsriverIncomingTokenUrl, method = HttpMethods.POST, headers = Nil, entity = HttpEntity(incomingResponse.toJson.toString))) map {
		case HttpResponse(StatusCodes.OK, _, _, _) => log.info(s"Successfully send data to $newsriverIncomingTokenUrl with data ${incomingResponse.toJson.toString}")

		// Something went wrong while sending information to incoming token url
		case _ => log.warning(s"Could not send data to token url $newsriverIncomingTokenUrl")
	}


	/**
		* Build from a list of NewsriverResponses a single SlashResponse
		*
		* @param newsriverResponses The List of newsriver responses
		* @return A single optional slash response
		*/
	private def buildSlashAndIncomingWebhookResponse(newsriverResponses: List[NewsriverResponse], request: SlashCommandRequest): (Option[SlashResponse], Option[IncomingResponse]) = {
		newsriverResponses match {
			case x: List[NewsriverResponse] if x nonEmpty => val responses = createSlashResponse(x, request)
				(Some(responses._1), Some(responses._2))

			case x: List[NewsriverResponse] if x isEmpty => (Some(SlashResponse("ephemeral", "nothing found", List())), None)
		}
	}

	private def createSlashResponse(newsriverResponses: List[NewsriverResponse], request: SlashCommandRequest): (SlashResponse, IncomingResponse) = {
		val attachments = for {
			r <- newsriverResponses
			e <- r.elements
		} yield SlashResponseAttachment(r.title, r.title, r.url, r.text, e.url,
			List(SlashResponseField(r.website.domainName, "Ranking: " + r.website.rankingGlobal.toString)),
			rankingColor(r.website.rankingGlobal))

		val responseText = s"${request.username} searched for ${request.text}\nFound ${attachments.size} articles"
		(SlashResponse(newsriverResponseType, responseText, List()), IncomingResponse(responseText, attachments))
	}

	/**
		* Get a ranking color for a single news
		*
		* @param globalRanking The global ranking value
		* @return A hex color code on global ranking
		*/
	private def rankingColor(globalRanking: Int) = {
		globalRanking match {
			case x if x < 10000 => "#FF5000"
			case x if x < 30000 => "#FFAF00"
			case x if x < 50000 => "#764FA5"
			case x if x < 75000 => "#36a64f"
			case x if x < 90000 => "#36a6Cf"
			case _ => "#000000"
		}
	}

	private def sanitizeTitle(title: String): String = {
		URLEncoder.encode(title, "utf8")
	}

	/**
		* Get the SlashResponse result for this integration
		* Converts the string to a json string and serialize it to the NewsriverResponse
		*
		* @param request The SlashRequest to build the response
		* @return A Future of SlashResponse
		*/
	override def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]] = {
		val url = buildNewsriverUrl(sanitizeTitle(request.text))
		val response = getResponse(url)

		response.map { x =>
			try {
				x.parseJson.convertTo[List[NewsriverResponse]] match {
					case x: List[NewsriverResponse] => buildSlashAndIncomingWebhookResponse(x, request) match {
						case (a, Some(b)) => sendNewsriverResultToIncomingWebhook(b); a
						case (a, b) => a
					}

				}
			} catch {
				case ex: Exception => buildSlashAndIncomingWebhookResponse(List(), request)._1
			}
		}
	}
}
