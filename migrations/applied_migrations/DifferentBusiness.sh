#!/bin/bash

echo ""
echo "Applying migration DifferentBusiness"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /differentBusiness                       controllers.DifferentBusinessController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "differentBusiness.title = differentBusiness" >> ../conf/messages.en
echo "differentBusiness.heading = differentBusiness" >> ../conf/messages.en

echo "Migration DifferentBusiness completed"
