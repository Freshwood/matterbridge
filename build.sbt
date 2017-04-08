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
  "org.scalatest" % "scalatest_2.11" % scalaTestVersion % "test",
  "org.scalamock" % "scalamock-scalatest-support_2.11" % scalaMockVersion % "test"
)

val serviceLibs = Seq(
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "net.ruippeixotog" % "scala-scraper_2.11" % scalaScrapperVersion
)

lazy val codeQualitySettings = Seq(
  ivyLoggingLevel := UpdateLogging.DownloadOnly,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8", // yes, this is 2 args
    "-feature",
    "-unchecked",
    "-Ywarn-numeric-widen",
    //"-Ywarn-value-discard",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Xfuture"
  ),
  coverageEnabled in Test := true,
  coverageMinimum in Test := 80,
  coverageFailOnMinimum in Test := false,
  coverageHighlighting in Test := true
)

def settings(projectName: String) =
  Seq(
    organization := "freshsoft",
    name := projectName,
    version := "1.5.0",
    scalaVersion := "2.12.1",
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  ) ++ codeQualitySettings

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
