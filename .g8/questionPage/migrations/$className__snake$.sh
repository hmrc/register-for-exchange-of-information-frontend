#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$className;format="decap"$                        controllers.$className$Controller.onPageLoad(mode: models.Mode = models.NormalMode)" >> ../conf/app.routes
echo "POST       /$className;format="decap"$                        controllers.$className$Controller.onSubmit(mode: models.Mode = models.NormalMode)" >> ../conf/app.routes

echo "GET        /change$className$                  controllers.$className$Controller.onPageLoad(mode: models.Mode = models.CheckMode)" >> ../conf/app.routes
echo "POST       /change$className$                  controllers.$className$Controller.onSubmit(mode: models.Mode = models.CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.$field1Name$ = $field1Name$" >> ../conf/messages.en
echo "$className;format="decap"$.$field2Name$ = $field2Name$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.$field1Name$.required = Enter $field1Name$" >> ../conf/messages.en
echo "$className;format="decap"$.error.$field2Name$.required = Enter $field2Name$" >> ../conf/messages.en
echo "$className;format="decap"$.error.$field1Name$.length = $field1Name$ must be $field1MaxLength$ characters or less" >> ../conf/messages.en
echo "$className;format="decap"$.error.$field2Name$.length = $field2Name$ must be $field2MaxLength$ characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$UserAnswersEntry: Arbitrary[(pages.$className$Page.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[pages.$className$Page.type]";\
    print "        value <- arbitrary[models.$className$].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$Page: Arbitrary[pages.$className$Page.type] =";\
    print "    Arbitrary(pages.$className$Page)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$: Arbitrary[models.$className$] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        $field1Name$ <- arbitrary[String]";\
    print "        $field2Name$ <- arbitrary[String]";\
    print "      } yield models.$className$($field1Name$, $field2Name$)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[($className$Page.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/extends RowBuilder/ {\
     print;\
     print "";\
     print "  def $className;format="decap"$: Option[Row] = userAnswers.get(pages.$className$Page) map {";\
     print "      answer =>";\
     print "        toRow(";\
     print "          msgKey = \"$className;format="decap"$\",";\
     print "          content = msg\"site.edit\",";\
     print "          href = routes.$className$Controller.onPageLoad(CheckMode).url,";\
     print "        )";\
     print "    }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving tests"
mv ../generated-test/controllers/$className$ControllerSpec.scala ../test/controllers/
mv ../generated-test/forms/$className$FormProviderSpec.scala ../test/forms/
mv ../generated-test/pages/$className$PageSpec.scala ../test/pages/

echo "Migration $className;format="snake"$ completed"
