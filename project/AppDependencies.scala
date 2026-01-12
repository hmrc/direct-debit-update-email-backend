import sbt.*

object AppDependencies {

  private val playVersion = "-play-30"

  private val bootstrapVersion = "10.5.0"
  private val hmrcMongoVersion = "2.11.0"
  private val enumeratumVersion = "1.9.0"
  private val enumeratumPlayVersion = "1.9.2"
  private val catsVersion = "2.13.0"
  private val cryptoVersion = "8.4.0"
  private val playJsonDerivedCodesVersion = "10.1.0"
  private val chimneyVersion = "1.8.2"
  private val circeVersion = "0.14.15"

  lazy val microserviceDependencies: Seq[ModuleID] = {

    val compile = Seq(
      // format: OFF
      "uk.gov.hmrc"          %% s"internal-auth-client$playVersion" % "4.3.0",
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
    "io.circe"          %% "circe-core"                    % circeVersion,
    "io.circe"          %% "circe-generic"                 % circeVersion,
    "io.circe"          %% "circe-parser"                  % circeVersion,
    "org.typelevel"     %% "cats-core"                     % catsVersion
  // format: ON
  )
}
