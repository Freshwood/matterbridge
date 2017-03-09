import sbt.Keys._

val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.4"
val scalaTestVersion = "2.2.6"
val logbackVersion = "1.1.3"
val scalaScrapperVersion = "1.0.0"
val scalaMockVersion = "3.2.2"
val flyway = "3.2.1"

val database: Seq[ModuleID] = Seq(
  "com.typesafe.play" %% "anorm" % "2.5.1",
  "org.flywaydb" % "flyway-core" % flyway,
  "org.postgresql" % "postgresql" % "9.4.1211",
  "com.h2database" % "h2" % "1.4.192" % "test"
)

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging)
  .settings(
    organization := "freshsoft",
    name := "matter-bridge",
    version := "1.5.0",
    scalaVersion := "2.11.8",
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "net.ruippeixotog" %% "scala-scraper" % scalaScrapperVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % scalaMockVersion % "test"
    ) ++ database,
    parallelExecution := false,
    mappings in Universal += {
      // we are using the application.conf as default application.conf
      // the user can override settings here
      val conf = (resourceDirectory in Compile).value / "application.conf"
      conf -> "conf/application.conf"
    }
  )
