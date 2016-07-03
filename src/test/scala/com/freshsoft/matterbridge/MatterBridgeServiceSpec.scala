package com.freshsoft.matterbridge

import akka.http.scaladsl.model.FormData
import com.freshsoft.matterbridge.server.MatterBridgeService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

/**
	* Test the matter bridge service integration
	*/
class MatterBridgeServiceSpec
	extends WordSpec
		with Matchers
		with ScalaFutures {

	val wrongFormData = FormData(Map("command" -> "command"))
	val rightFormData = FormData(Map(
		"command" -> "codinglove",
		"user_name" -> "somename",
		"text" -> "testtext"))

	val service = new MatterBridgeService

	"The matter bridge service" should {

		"Return nothing when a wrong command was send" in {
			whenReady(service.matterBridgeIntegration(wrongFormData)) {
				result => result shouldBe None
			}
		}
	}
}
