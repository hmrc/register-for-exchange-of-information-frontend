#!/bin/bash

echo ""
echo "Applying migration UnableToChangeBusiness"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /unableToChangeBusiness                       controllers.UnableToChangeBusinessController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "unableToChangeBusiness.title = unableToChangeBusiness" >> ../conf/messages.en
echo "unableToChangeBusiness.heading = unableToChangeBusiness" >> ../conf/messages.en

echo "Migration UnableToChangeBusiness completed"
