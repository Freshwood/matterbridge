import sbt.Keys._

val akkaVersion = "2.4.7"
val scalaTestVersion = "2.2.6"
val logbackVersion = "1.1.3"

lazy val root = (project in file(".")).
	enablePlugins(JavaServerAppPackaging).
	settings(
		organization := "freshsoft",
		name := "matter-bridge",
		version := "1.0",
		scalaVersion := "2.11.8",
		resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
			"ch.qos.logback" % "logback-classic" % logbackVersion,
			"com.typesafe.akka" %% "akka-http-core" % akkaVersion,
			"com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
			"com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
			"com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test",
			"net.ruippeixotog" %% "scala-scraper" % "1.0.0",
			"org.scalatest" %% "scalatest" % scalaTestVersion % "test",
			"org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
		)
	)

