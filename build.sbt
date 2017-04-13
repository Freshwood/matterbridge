import sbt.Keys._

val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.4"
val scalaTestVersion = "2.2.6"
val logbackVersion = "1.1.3"
val scalaScrapperVersion = "1.0.0"
val scalaMockVersion = "3.2.2"
val flyway = "3.2.1"

val database: Seq[ModuleID] = Seq(
  "org.scalikejdbc" %% "scalikejdbc-async" % "0.7.+",
  "com.github.mauricio" %% "postgresql-async" % "0.2.+",
  "org.flywaydb" % "flyway-core" % flyway,
  "org.postgresql" % "postgresql" % "42.0.0",
  "com.h2database" % "h2" % "1.4.192" % "test"
)

val testLibs: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % scalaMockVersion % "test"
)

val serviceLibs = Seq(
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "net.ruippeixotog" %% "scala-scraper" % scalaScrapperVersion
)

def settings(projectName: String) = Seq(
  organization := "freshsoft",
  name := projectName,
  version := "2.0.0-prelease",
  scalaVersion := "2.11.8",
  resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
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
  ) dependsOn data aggregate (model, data)
