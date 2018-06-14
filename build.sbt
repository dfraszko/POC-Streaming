version := "1.0"
scalaVersion := "2.11.8"

enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  guice,
  ws,
  specs2                % Test,
  "org.scalaz"          %% "scalaz-core" % "7.2.7",
  "io.codearte.jfairy"  % "jfairy" % "0.5.9",
  "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.3"
//"net.codingwell"      %% "scala-guice" % "4.1.0",
//  "com.typesafe.play"   %% "play-slick" % "3.0.2",
//  "com.github.tminglei" %% "slick-pg" % "0.16.1",
//  "com.github.tminglei" %% "slick-pg_jts" % "0.16.1",
//  "com.github.tminglei" %% "slick-pg_play-json" % "0.16.1",
//  "org.postgresql"      % "postgresql" % "42.1.4" % Runtime,
//  "org.webjars"         % "bootstrap" % "3.3.7-1",
//  "org.webjars.bower"   % "angular" % "1.6.6",
//  "org.webjars.bower"   % "angular-route" % "1.6.6",
//  "org.webjars.bower"   % "angular-resource" % "1.6.6",
//  "org.webjars.bower"   % "angular-bootstrap" % "2.5.0",
//  "org.webjars.bower"   % "angular-websocket" % "2.0.0"
)

scalacOptions ++= Seq(
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

PlayKeys.playDefaultPort := 8080

dockerBaseImage := "openjdk:8u171-jre-slim"
dockerExposedPorts := Seq(PlayKeys.playDefaultPort.value)
dockerUpdateLatest := true
dockerEntrypoint ++= Seq(
  "-Dhttp.port=" + PlayKeys.playDefaultPort.value.toString,
  "-Dhttp.address=0.0.0.0"
)
