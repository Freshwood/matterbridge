package com.freshsoft.matterbridge.client.newsriver

import java.net.URLEncoder

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model._
import akka.util.ByteString
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.{ISlashCommandJsonSupport, NewsriverResponse, SlashResponse}
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.WithConfig
import spray.json._

import scala.concurrent.Future
import scala.language.postfixOps

/**
	* Created by Freshwood on 27.07.2016.
	*/
object NewsriverIntegration extends IMatterBridgeResult
	with WithConfig
	with WithActorContext
	with ISlashCommandJsonSupport{

	val log = Logging.getLogger(system, this)

	val apiToken = "sBBqsGXiYgF0Db5OV5tAw9cKkG-R9HP8i_Hw0VCYICEOnIvuIlisyP67o0v1pThT"

	val apiHeader = HttpHeader.parse("authorization", apiToken) match {
		case Ok(header, errors)=> header
		case _ => throw new IllegalArgumentException("Could not parse api token to header")
	}

	private def buildNewsriverUrl(title: String) = {
		s"https://api.newsriver.io/v2/search?query=title:$title&limit=5"
	}

	private def getResponse(url: String) = {

		val request = HttpRequest(HttpMethods.GET, url, collection.immutable.Seq(apiHeader))

		 Http().singleRequest(request).flatMap {
			 case HttpResponse(StatusCodes.OK, headers, entity, _) =>
				 entity.dataBytes.runFold(ByteString(""))(_ ++ _).map {
					 x => x.decodeString("UTF-8")
				 }

			 // Just return nothing when the site is down or we don't receive what we want
			 // The further handling is above
			 case _ => Future {""}
		 }
	}

	private def buildSlashResponse(newsriverResponses: List[NewsriverResponse]): Option[SlashResponse] = {
		newsriverResponses match {
			case x: List[NewsriverResponse] if x nonEmpty =>
				Some(SlashResponse(newsriverResponseType, buildSlashResponseText(x)))

			case x: List[NewsriverResponse] if x isEmpty =>
				Some(SlashResponse("ephemeral", "nothing found"))
		}
	}

	private def buildSlashResponseText(newsriverResponses: List[NewsriverResponse]): String = {
		(for {
			r <- newsriverResponses
			e <- r.elements
		} yield s"${r.title}\nUrl: ${r.url}\nImage: ${e.url}\nDiscover Date: ${r.discoverDate}\n").mkString
	}

	private def sanitizeTitle(title: String): String = {
		URLEncoder.encode(title, "utf8")
	}

	/**
		* Get the SlashResponse result for this integration
		*
		* @param request The SlashRequest to build the response
		* @return A Future of SlashResponse
		*/
	override def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]] = {
		val url = buildNewsriverUrl(sanitizeTitle(request.text))
		val response = getResponse(url)

		response.map {
			x => x.parseJson.convertTo[List[NewsriverResponse]] match {
				case x: List[NewsriverResponse] => buildSlashResponse(x)
			}
		}
	}
}
