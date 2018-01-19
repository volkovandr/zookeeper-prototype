name := "zookeeper-prototype"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "org.apache.curator" % "curator-recipes" % "4.0.0" exclude("org.apache.zookeeper", "zookeeper") exclude("log4j", "*")
libraryDependencies += "org.apache.zookeeper" % "zookeeper" % "3.4.11" exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12")

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging"           % "3.7.2",
  "ch.qos.logback"              % "logback-classic"         % "1.2.3",
  "org.slf4j"                   % "log4j-over-slf4j"        % "1.7.25"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-stream"             % "2.5.8",
  "com.typesafe.akka"          %% "akka-testkit"            % "2.5.8")


