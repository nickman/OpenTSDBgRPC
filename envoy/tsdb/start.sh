#!/bin/bash
# -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044
AGENT=/home/nwhitehead/hprojects/jvm-name/target/jvm-name-1.0-SNAPSHOT.jar
PORT=$1
export GRPC_SERVER_PORT=$2
OPTIONS="--auto-metric --auto-tagk --auto-tagv"
OPTIONS="$OPTIONS --plugin-path=$HOME/plugins --rpc-plugins=net.opentsdb.grpc.server.IsolatedGRPCPlugin --startup-plugin=net.opentsdb.consul.IsolatedConsulPlugin"
OPTIONS="$OPTIONS --ignore-existing-pid --startup-plugin-enabled "
OPTIONS="$OPTIONS --port=$PORT"
java  -Dnet.opentsdb.tools=DEBUG -Xmx1024m -Xms1024m -jar target/opentsdb-fatjar-2.4.0-SNAPSHOT-fat.jar tsd $OPTIONS
