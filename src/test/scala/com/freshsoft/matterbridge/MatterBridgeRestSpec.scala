package com.freshsoft.matterbridge

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.freshsoft.matterbridge.routing.MatterBridgeRoute
import org.scalatest.{Matchers, WordSpec}

/**
	* The matterbridge REST service tests
	*/
class MatterBridgeRestSpec
	extends WordSpec
		with Matchers
		with ScalatestRouteTest {

	val routes = new MatterBridgeRoute().routes


	"The matter bridge service" should {

		"return not found on root path" in {
			Get() ~> Route.seal(routes) ~> check {
				status shouldBe StatusCodes.NotFound
				responseAs[String] shouldEqual "The requested resource could not be found."
			}
		}

		"return not found on api path" in {
			Get("/api") ~> Route.seal(routes) ~> check {
				status shouldBe StatusCodes.NotFound
				responseAs[String] shouldEqual "The requested resource could not be found."
			}
		}

		"return method not allowed on api/matterbridge with GET" in {
			Get("/api/matterbridge") ~> Route.seal(routes) ~> check {
				status shouldBe StatusCodes.MethodNotAllowed
				responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
			}
		}

		"leave GET requests to other paths unhandled" in {
			// tests:
			Get("/kermit") ~> routes ~> check {
				handled shouldBe false
			}
		}

		"disallow Get on matterbridge path" in {
			Get("/api/matterbridge") ~> Route.seal(routes) ~> check {
				status === StatusCodes.MethodNotAllowed
				responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
			}
		}

		"allow Posts on matterbridge path" in {
			Post("/api/matterbridge") ~> Route.seal(routes) ~> check {
				status === StatusCodes.OK
			}
		}
	}
}
