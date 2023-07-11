import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val playVersion = s"-play-28"

  private val bootstrapVersion = "7.19.0"
  private val hmrcMongoVersion = "1.3.0"
  private val enumeratumVersion = "1.7.0"
  private val catsVersion = "2.9.0"
  private val cryptoVersion = "7.3.0"
  private val hmrcJsonEncryptionVersion = "5.1.0-play-28"
  private val playJsonDerivedCodesVersion = "7.0.0"
  private val chimneyVersion = "0.7.5"

  lazy val microserviceDependencies: Seq[ModuleID] = {

    val compile = Seq(
      // format: OFF
      "uk.gov.hmrc"          %% s"internal-auth-client$playVersion" % "1.6.0",
      "uk.gov.hmrc"          %% s"bootstrap-backend$playVersion"    % bootstrapVersion,
      "com.beachape"         %% "enumeratum"                        % enumeratumVersion,
      "io.scalaland"         %% "chimney"                           % chimneyVersion
    // format: ON
    )

    val test = Seq(
      // format:: OFF
      "uk.gov.hmrc" %% s"bootstrap-test$playVersion" % bootstrapVersion,
      "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test$playVersion" % hmrcMongoVersion
    // format: ON
    ).map(_ % Test)

    compile ++ test
  }

  lazy val corJourneyDependencies: Seq[ModuleID] = Seq(
    // format:: OFF
    //WARN! - The version of `auth-client` has to be exact!
    //make sure it's version is the same as version in bootstrap (`uk.gov.hmrc:bootstrap-backend-play-xx_x.xx:xxx`)
    //run `essttp-backend/dependencyTree::toFile deps.txt -f` and look for that line:
    // +-uk.gov.hmrc:auth-client_2.12:3.0.0-play-27 (evicted by: 5.1.0-play-27)
    //the correct version in this time was `3.0.0`
    "uk.gov.hmrc" %% "auth-client" % "6.1.0-play-28",
    "uk.gov.hmrc" %% s"bootstrap-common$playVersion" % AppDependencies.bootstrapVersion % Provided,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo$playVersion" % AppDependencies.hmrcMongoVersion,
    "uk.gov.hmrc" %% "json-encryption" % hmrcJsonEncryptionVersion,
    "uk.gov.hmrc" %% s"crypto-json$playVersion" % AppDependencies.cryptoVersion,
    "com.typesafe.play" %% "play" % play.core.PlayVersion.current % Provided,
    "com.beachape" %% "enumeratum-play" % AppDependencies.enumeratumVersion,
    "org.julienrf" %% "play-json-derived-codecs" % AppDependencies.playJsonDerivedCodesVersion, //choose carefully
    "org.typelevel" %% "cats-core" % catsVersion
  // format: ON
  )
}
