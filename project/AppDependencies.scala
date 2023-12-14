import sbt.*

object AppDependencies {

  private val playVersion = "-play-30"

  private val bootstrapVersion = "8.2.0"
  private val hmrcMongoVersion = "1.6.0"
  private val enumeratumVersion = "1.7.3"
  private val enumeratumPlayVersion = "1.8.0"
  private val catsVersion = "2.10.0"
  private val cryptoVersion = "7.6.0"
  private val playJsonDerivedCodesVersion = "10.1.0"
  private val chimneyVersion = "0.8.3"

  lazy val microserviceDependencies: Seq[ModuleID] = {

    val compile = Seq(
      // format: OFF
      "uk.gov.hmrc"          %% s"internal-auth-client$playVersion" % "1.9.0",
      "uk.gov.hmrc"          %% s"bootstrap-backend$playVersion"    % bootstrapVersion,
      "com.beachape"         %% "enumeratum"                        % enumeratumVersion,
      "io.scalaland"         %% "chimney"                           % chimneyVersion
     // format: ON
    )

    val test = Seq(
      // format: OFF
      "uk.gov.hmrc"       %% s"bootstrap-test$playVersion"  % bootstrapVersion,
      "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test$playVersion" % hmrcMongoVersion
      // format: ON
    ).map(_ % Test)

    compile ++ test
  }

  lazy val corJourneyDependencies: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc"       %% s"bootstrap-common$playVersion" % AppDependencies.bootstrapVersion % Provided,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo$playVersion"       % AppDependencies.hmrcMongoVersion,
    "uk.gov.hmrc"       %% s"crypto-json$playVersion"      % AppDependencies.cryptoVersion,
    "com.beachape"      %% "enumeratum-play"               % AppDependencies.enumeratumPlayVersion,
    "org.julienrf"      %% "play-json-derived-codecs"      % AppDependencies.playJsonDerivedCodesVersion,
    "org.typelevel"     %% "cats-core"                     % catsVersion
    // format: ON
  )
}
