import sbt.Keys._
import sbt._
import wartremover.Wart
import wartremover.WartRemover.autoImport.{Warts, wartremoverErrors, wartremoverExcluded}

object WartRemoverSettings {

  lazy val wartRemoverSettings =
    Seq(
      (Compile / compile / wartremoverErrors) ++= Warts.allBut(
        Wart.DefaultArguments,
        Wart.ImplicitConversion,
        Wart.ImplicitParameter,
        Wart.Nothing,
        Wart.Overloading,
        Wart.Throw,
        Wart.ToString,
        Wart.FinalVal // chimney's type class 'TransformerOps.into' uses final def
      ),
      Test / compile / wartremoverErrors --= Seq(
        Wart.Any,
        Wart.Equals,
        Wart.GlobalExecutionContext,
        Wart.Null,
        Wart.NonUnitStatements,
        Wart.PublicInference
      ),
      wartremoverExcluded ++= (baseDirectory.value ** "*.sc").get
    )

}
