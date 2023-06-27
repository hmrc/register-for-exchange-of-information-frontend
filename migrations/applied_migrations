#!/bin/bash

echo ""
echo "Applying migration ReporterType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /reporterType                        controllers.ReporterTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /reporterType                        controllers.ReporterTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeReporterType                  controllers.ReporterTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeReporterType                  controllers.ReporterTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "reporterType.title = reporterType" >> ../conf/messages.en
echo "reporterType.heading = reporterType" >> ../conf/messages.en
echo "reporterType.option1 = Option 1" >> ../conf/messages.en
echo "reporterType.option2 = Option 2" >> ../conf/messages.en
echo "reporterType.checkYourAnswersLabel = reporterType" >> ../conf/messages.en
echo "reporterType.error.required = Select reporterType" >> ../conf/messages.en
echo "reporterType.change.hidden = ReporterType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryReporterTypeUserAnswersEntry: Arbitrary[(ReporterTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ReporterTypePage.type]";\
    print "        value <- arbitrary[ReporterType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryReporterTypePage: Arbitrary[ReporterTypePage.type] =";\
    print "    Arbitrary(ReporterTypePage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryReporterType: Arbitrary[ReporterType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(ReporterType.values)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ReporterTypePage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration ReporterType completed"
