
name := "tdr-prototype-cookie-signing"

version := "0.1"

scalaVersion := "2.13.6"

val circeVersion = "0.14.1"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-cloudfront" % "1.12.47"
libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"
libraryDependencies += "com.auth0" % "java-jwt" % "3.18.1"
libraryDependencies += "com.typesafe" % "config" % "1.4.1"
libraryDependencies += "io.circe" %% "circe-core" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser" % circeVersion

assemblyJarName in assembly := "prototype-cookie-signing.jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
