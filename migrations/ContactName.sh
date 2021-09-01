#!/bin/bash

echo ""
echo "Applying migration ContactName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /contactName                        controllers.ContactNameController.onPageLoad(mode: models.Mode = models.NormalMode)" >> ../conf/app.routes
echo "POST       /contactName                        controllers.ContactNameController.onSubmit(mode: models.Mode = models.NormalMode)" >> ../conf/app.routes

echo "GET        /changeContactName                  controllers.ContactNameController.onPageLoad(mode: models.Mode = models.CheckMode)" >> ../conf/app.routes
echo "POST       /changeContactName                  controllers.ContactNameController.onSubmit(mode: models.Mode = models.CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "contactName.title = contactName" >> ../conf/messages.en
echo "contactName.heading = contactName" >> ../conf/messages.en
echo "contactName.checkYourAnswersLabel = contactName" >> ../conf/messages.en
echo "contactName.error.required = Enter contactName" >> ../conf/messages.en
echo "contactName.error.length = ContactName must be 1000 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryContactNameUserAnswersEntry: Arbitrary[(pages.ContactNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[pages.ContactNamePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryContactNamePage: Arbitrary[pages.ContactNamePage.type] =";\
    print "    Arbitrary(pages.ContactNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ContactNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/extends RowBuilder/ {\
     print;\
     print "";\
     print "  def contactName: Option[Row] = userAnswers.get(pages.ContactNamePage) map {";\
     print "      answer =>";\
     print "        toRow(";\
     print "          msgKey = \"contactName\",";\
     print "          content = msg\"site.edit\",";\
     print "          href = routes.ContactNameController.onPageLoad(CheckMode).url,";\
     print "        )";\
     print "    }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving tests"
mv ../generated-test/controllers/ContactNameControllerSpec.scala ../test/controllers/
mv ../generated-test/forms/ContactNameFormProviderSpec.scala ../test/forms/
mv ../generated-test/pages/ContactNamePageSpec.scala ../test/pages/

echo "Migration ContactName completed"
