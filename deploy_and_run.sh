# Build jar and send it to the PingFederate's deploy package
mvn clean package
rm /home/luiky/Documents/ping/pingfederate/server/default/deploy/native-pingfederate-1.0-SNAPSHOT.jar
cp /home/luiky/Repositories/native-pingfederate/target/native-pingfederate-1.0-SNAPSHOT.jar /home/luiky/Documents/ping/pingfederate/server/default/deploy/native-pingfederate-1.0-SNAPSHOT.jar
~/Documents/ping/pingfederate/bin/./run.sh