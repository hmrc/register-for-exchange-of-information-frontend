import sbt._

object AppDependencies {
  import play.core.PlayVersion

  private val bootstrapVersion = "7.22.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-28"    % "8.5.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"            % "1.3.0",
    "uk.gov.hmrc"       %% "domain"                        % "8.3.0-play-28",
    "org.typelevel"     %% "cats-core"                     % "2.1.1"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"    % bootstrapVersion,
    "org.scalatestplus"      %% "scalacheck-1-15"           % "3.2.10.0",
    "org.pegdown"             % "pegdown"                   % "1.6.0",
    "org.jsoup"               % "jsoup"                     % "1.14.3",
    "org.mockito"            %% "mockito-scala"             % "1.16.46",
    "com.github.tomakehurst"  % "wiremock-jre8"             % "2.26.0",
    "wolfendale"             %% "scalacheck-gen-regexp"     % "0.1.2",
    "com.vladsch.flexmark"    % "flexmark-all"              % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
