name := "choosenear.me web"

version := "1.0"

scalaVersion := "2.8.1"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "com.twitter" % "finagle-core" % "1.5.3",
  "org.scalaj" %% "scalaj-collection" % "1.1",
  "org.scalaj" %% "scalaj-http" % "0.2.7",
  "net.liftweb" %% "lift-json" % "2.3")

resolvers ++= Seq(
  "twitter.com" at "http://maven.twttr.com/")
