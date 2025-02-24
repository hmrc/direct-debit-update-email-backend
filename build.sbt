import uk.gov.hmrc.DefaultBuildSettings.scalaSettings
import uk.gov.hmrc.ShellPrompt
import wartremover.WartRemover.autoImport.wartremoverExcluded

val appName: String = "direct-debit-update-email-backend"

val appScalaVersion = "3.3.4"

lazy val scalaCompilerOptions = Seq(
  "-Xfatal-warnings",
  "-Wvalue-discard",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:strictEquality",
  // required in place of silencer plugin
  "-Wconf:msg=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

lazy val commonSettings = Seq[SettingsDefinition](
  majorVersion := 1,
  scalacOptions ++= scalaCompilerOptions,
  update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
  shellPrompt := ShellPrompt(version.value),
  buildInfoPackage := name.value.toLowerCase().replaceAllLiterally("-", ""),
  Compile / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task and not fail the build
  scalafmtOnCompile := true,
  scalaSettings,
  uk.gov.hmrc.DefaultBuildSettings.defaultSettings(),
  WartRemoverSettings.wartRemoverSettings,
  ScoverageSettings.scoverageSettings,
  SbtUpdatesSettings.sbtUpdatesSettings,
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(commonSettings *)
  .settings(
    scalaVersion := appScalaVersion,
    libraryDependencies ++= AppDependencies.microserviceDependencies,
    wartremoverExcluded ++= (Compile / routes).value
  )
  .settings(PlayKeys.playDefaultPort := 10802)
  .settings(resolvers += Resolver.jcenterRepo)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .dependsOn(corJourney)
  .aggregate(corJourney)

/**
 * Collection Of Routines - the common journey
 */
lazy val corJourney = Project(appName + "-cor-journey", file("cor-journey"))
  .settings(commonSettings *)
  .settings(
    scalaVersion := appScalaVersion,
    libraryDependencies ++= AppDependencies.corJourneyDependencies
  )
