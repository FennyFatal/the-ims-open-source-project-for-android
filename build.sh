ant
rm -R IMSTestClient-debug/
rm classes.dex
rm -R IMSTestClient/
baksmali test-client/bin/IMSTestClient-debug.apk -o IMSTestClient-debug/
cp -R smaliadds/* IMSTestClient-debug/
unzip -d IMSTestClient test-client/bin/IMSTestClient-debug.apk
smali IMSTestClient-debug -o IMSTestClient/classes.dex
cd IMSTestClient/
zip -r ../IMSTestClient.apk *
cd ..
java -classpath bin/testsign.jar testsign IMSTestClient.apk IMSTestClient-signed.apk
../dist.sh IMSTestClient-signed.apk