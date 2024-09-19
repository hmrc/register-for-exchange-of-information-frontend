import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.6.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"    % "10.11.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.2.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"            % "2.2.0",
    "uk.gov.hmrc"       %% "domain-play-30"                        % "10.0.0",
    "org.typelevel"     %% "cats-core"                     % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"    % bootstrapVersion,
    "org.scalatestplus"      %% "scalacheck-1-15"           % "3.2.11.0",
    "org.mockito"            %% "mockito-scala"             % "1.17.31",
    "wolfendale"             %% "scalacheck-gen-regexp"     % "0.1.2",
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
