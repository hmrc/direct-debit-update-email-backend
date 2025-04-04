import com.timushev.sbt.updates.UpdatesKeys.dependencyUpdates
import com.timushev.sbt.updates.UpdatesPlugin.autoImport.{dependencyUpdatesFailBuild, dependencyUpdatesFilter, moduleFilterRemoveValue}
import sbt.Keys.*
import sbt.*

object SbtUpdatesSettings {

  lazy val sbtUpdatesSettings = Seq(
    dependencyUpdatesFailBuild := true,
    (Compile / compile) := ((Compile / compile) dependsOn dependencyUpdates).value,
    // I have had to add enumeratum to the ignore list, due to:
    // java.lang.NoSuchMethodError: 'scala.Option play.api.libs.json.JsBoolean$.unapply(play.api.libs.json.JsBoolean)'
    // error on 1.7.2
    dependencyUpdatesFilter -= moduleFilter("com.beachape", "enumeratum"),
    dependencyUpdatesFilter -= moduleFilter("com.beachape", "enumeratum-play")
  )

}
