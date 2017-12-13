import sbt.Keys._

lazy val library = new {

  object Version {
    val akkaVersion = "2.5.8"
    val akkaHttpVersion = "10.0.11"
    val scalaTestVersion = "2.2.6"
    val logbackVersion = "1.1.3"
    val scalaScraperVersion = "2.0.0"
    val scalaMockVersion = "3.6.0"
    val postgres = "42.0.0"
    val flyway = "3.2.1"
    val scalikeJdbc = "0.7.+"
    val postgresAsync = "0.2.+"
    val h2 = "1.4.192"
    val scalaModules = "1.0.6"
    val restClient = "0.1.0"
  }

  val scalikeJdbc = "org.scalikejdbc" %% "scalikejdbc-async" % Version.scalikeJdbc
  val postgresAsync = "com.github.mauricio" %% "postgresql-async" % Version.postgresAsync
  val flyway = "org.flywaydb" % "flyway-core" % Version.flyway
  val postgres = "org.postgresql" % "postgresql" % Version.postgres
  val h2 = "com.h2database" % "h2" % Version.h2

  val scalaModules = "org.scala-lang.modules" %% "scala-xml" % Version.scalaModules
  val restClient = "net.softler" %% "akka-http-rest-client" % Version.restClient
  val akkaLogging = "com.typesafe.akka" %% "akka-slf4j" % Version.akkaVersion
  val logging = "ch.qos.logback" % "logback-classic" % Version.logbackVersion
  val akka = "com.typesafe.akka" %% "akka-actor" % Version.akkaVersion
  val akkaStreams = "com.typesafe.akka" %% "akka-stream" % Version.akkaVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttpVersion
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % Version.akkaHttpVersion
  val akkaSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttpVersion
  val scrapper = "net.ruippeixotog" %% "scala-scraper" % Version.scalaScraperVersion
  val akkaTestKit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttpVersion

  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTestVersion
  val scalaMockTest = "org.scalamock" %% "scalamock-scalatest-support" % Version.scalaMockVersion
}

val database: Seq[ModuleID] = Seq(
  library.scalikeJdbc,
  library.postgresAsync,
  library.flyway,
  library.postgres,
  library.h2
)

val testLibs: Seq[ModuleID] = Seq(
  library.akkaTestKit % Test,
  library.scalaTest % Test,
  library.scalaMockTest
)

val serviceLibs: Seq[ModuleID] = Seq(
  library.scalaModules,
  library.restClient,
  library.akkaLogging,
  library.logging,
  library.akka,
  library.akkaStreams,
  library.akkaHttp,
  library.akkaHttpCore,
  library.akkaSprayJson,
  library.scrapper
)

def settings(projectName: String) = Seq(
  organization := "softler.net",
  name := projectName,
  version := "2.1.0",
  organizationName := "Tobias Frischholz",
  scalaVersion := "2.12.4",
  startYear := Some(2016),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfatal-warnings",
    "-Yno-adapted-args",
    "-Xfuture"
  ),
  resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.bintrayRepo("freshwood", "maven")
  )
)

lazy val model = (project in file("modules/model"))
  .settings(settings("matter-bridge-model"), libraryDependencies ++= serviceLibs)

lazy val data = (project in file("modules/data")).settings(
  settings("matter-bridge-data"),
  libraryDependencies ++= database
) dependsOn model

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging)
  .settings(
    settings("matter-bridge"),
    libraryDependencies ++= testLibs,
    parallelExecution := false,
    mappings in Universal += {
      // we are using the application.conf as default application.conf
      // the user can override settings here
      val conf = (resourceDirectory in Compile).value / "application.conf"
      conf -> "conf/application.conf"
    }
  ) dependsOn data aggregate(model, data)
