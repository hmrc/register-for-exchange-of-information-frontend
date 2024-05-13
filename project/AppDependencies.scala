import sbt._

object AppDependencies {
  import play.core.PlayVersion

  private val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"    % "8.5.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"            % "1.8.0",
    "uk.gov.hmrc"       %% "domain-play-30"                        % "9.0.0",
    "org.typelevel"     %% "cats-core"                     % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"    % bootstrapVersion,
    "org.scalatestplus"      %% "scalacheck-1-15"           % "3.2.11.0",
    "org.pegdown"             % "pegdown"                   % "1.6.0",
    "org.jsoup"               % "jsoup"                     % "1.17.2",
    "org.mockito"            %% "mockito-scala"             % "1.17.31",
    "wolfendale"             %% "scalacheck-gen-regexp"     % "0.1.2",
    "com.vladsch.flexmark"    % "flexmark-all"              % "0.64.8"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
