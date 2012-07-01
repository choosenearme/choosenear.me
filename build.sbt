name := "choosenearme"

version := "1.0"

scalaVersion := "2.9.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked")

libraryDependencies ++= Seq(
  "com.twitter" % "finagle-core" % "5.3.0",
  "com.twitter" % "finagle-http" % "5.3.0",
  "com.twitter" % "finagle-ostrich4" % "5.3.0",
  "com.twitter" % "util-eval" % "1.8.17",
  "org.scala-tools.time" % "time_2.9.1" % "0.5",
  "org.scalaj" % "scalaj-collection_2.9.1" % "1.2",
  "org.scalaj" % "scalaj-http_2.9.1" % "0.3.1",
  "net.liftweb" % "lift-json_2.9.1" % "2.4",
  "net.liftweb" % "lift-mongodb-record_2.9.1" % "2.4",
  "com.foursquare" % "rogue_2.9.1" % "1.1.8" intransitive())

resolvers ++= Seq(
  "twitter.com" at "http://maven.twttr.com/")

push <<= (assembly in Assembly, streams) map { (jar, s) =>
  val timestamp = {
    val f = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm")
    f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
    f.format(new java.util.Date)
  }
  val nameParts = jar.getName.split('.')
  val targetName = nameParts.patch(nameParts.size - 1, Seq(timestamp), 0).mkString(".")
  val targetPath = "/home/www/builds/" + targetName
  ("scp -p " + jar +" choosenear.me:" + targetPath) ! s.log
  ("ssh choosenear.me ln -fs " + targetName + " " + "/home/www/builds/root.jar") ! s.log
}

pushStatic <<= (streams) map { (s) =>
  ("scp -r static choosenear.me:/home/www") ! s.log
}

remoteApiRestart <<= (streams) map { (s) =>
  ("ssh choosenear.me sudo supervisorctl restart choosenearme") ! s.log
}

logLevel in push := Level.Debug
