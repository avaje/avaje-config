export POD_NAME=five-7d6d5bdf8-bsvpl
export POD_NAMESPACE=development
export POD_IP=1.2.3.4
export POD_VERSION=5.23

java -classpath target/test-classes:target/classes:/home/rob/.m2/repository-pv/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar io.avaje.config.ConfigEnvMain

#/home/rob/.m2/repository-pv/org/yaml/snakeyaml/1.25/snakeyaml-1.25.jar:/home/rob/.m2/repository-pv/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:/home/rob/.m2/repository-pv/org/avaje/composite/junit/1.1/junit-1.1.jar:/home/rob/.m2/repository-pv/junit/junit/4.12/junit-4.12.jar:/home/rob/.m2/repository-pv/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/home/rob/.m2/repository-pv/org/assertj/assertj-core/3.10.0/assertj-core-3.10.0.jar:/home/rob/.m2/repository-pv/com/h2database/h2/1.4.197/h2-1.4.197.jar:/home/rob/.m2/repository-pv/org/mockito/mockito-core/2.18.3/mockito-core-2.18.3.jar:/home/rob/.m2/repository-pv/net/bytebuddy/byte-buddy/1.8.5/byte-buddy-1.8.5.jar:/home/rob/.m2/repository-pv/net/bytebuddy/byte-buddy-agent/1.8.5/byte-buddy-agent-1.8.5.jar:/home/rob/.m2/repository-pv/org/objenesis/objenesis/2.6/objenesis-2.6.jar:/home/rob/apps/idea-IU-193.5233.102/lib/idea_rt.jar com.intellij.rt.execution.application.AppMainV2
