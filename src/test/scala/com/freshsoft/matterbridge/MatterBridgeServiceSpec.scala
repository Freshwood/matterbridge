package com.freshsoft.matterbridge

import akka.http.scaladsl.model.FormData
import com.freshsoft.matterbridge.client.MatterBridgeClient.CodingLoveIntegration
import com.freshsoft.matterbridge.entity.MattermostEntities.SlashResponse
import com.freshsoft.matterbridge.server.{IRest, MatterBridgeService}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{ExecutionContext, Future}

/**
	* Test the matter bridge service integration
	*/
class MatterBridgeServiceSpec
	extends WordSpec
		with Matchers
		with ScalaFutures
		with MockFactory
		with IRest {

	override implicit def executionContext: ExecutionContext = materializer.executionContext

	val wrongFormData = FormData(Map("command" -> "command"))
	val rightFormData = FormData(Map(
		"command" -> "codinglove",
		"user_name" -> "somename",
		"text" -> "testtext"))

	val service = new MatterBridgeService

	val slashResponseMock = new SlashResponse("some String", "Some Text")
	val slashResponseFutureMock = Future.successful[Option[SlashResponse]](Option(slashResponseMock))

	val codingLoveStub = stub[CodingLoveIntegration]
	(codingLoveStub.getResult _).when(*).returns(slashResponseFutureMock)

	"The matter bridge service" should {

		"Return nothing when a wrong command was send" in {
			whenReady(service.matterBridgeIntegration(wrongFormData)) {
				result => result shouldBe None
			}
		}
	}
}
