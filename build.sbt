
name := "tdr-prototype-cookie-signing"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"

assemblyJarName in assembly := "prototype-cookie-signing.jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
