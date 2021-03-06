import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"            % "3.13.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.11.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % "5.24.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"            % "0.68.0",
    "uk.gov.hmrc"       %% "logback-json-logger"           % "5.1.0",
    "uk.gov.hmrc"       %% "bootstrap-health-play-28"      % "5.21.0",
    "uk.gov.hmrc"       %% "play-nunjucks"                 % "0.35.0-play-28",
    "uk.gov.hmrc"       %% "play-nunjucks-viewmodel"       % "0.15.0-play-28",
    "uk.gov.hmrc"       %% "domain"                        % "8.0.0-play-28",
    "uk.gov.hmrc"       %% "emailaddress"                  % "3.6.0"
  )

  val test = Seq(
    "org.scalatest"          %% "scalatest"             % "3.2.10",
    "org.scalatestplus"      %% "scalacheck-1-15"       % "3.2.10.0",
    "org.scalatestplus.play" %% "scalatestplus-play"    % "5.1.0",
    "org.pegdown"             % "pegdown"               % "1.6.0",
    "org.jsoup"               % "jsoup"                 % "1.14.3",
    "com.typesafe.play"      %% "play-test"             % PlayVersion.current,
    "org.mockito"            %% "mockito-scala"         % "1.16.46",
    "com.github.tomakehurst"  % "wiremock-jre8"         % "2.26.0",
    "wolfendale"             %% "scalacheck-gen-regexp" % "0.1.2",
    "com.vladsch.flexmark"    % "flexmark-all"          % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
