name := "choosenearme"

version := "1.0"

scalaVersion := "2.8.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked")

libraryDependencies ++= Seq(
  "com.twitter" % "finagle-core" % "1.5.3",
  "com.twitter" % "util-eval" % "1.8.17",
  "org.scalaj" %% "scalaj-collection" % "1.1",
  "org.scalaj" %% "scalaj-http" % "0.2.7",
  "net.liftweb" %% "lift-json" % "2.3",
  "net.liftweb" %% "lift-mongodb-record" % "2.3",
  "com.foursquare" %% "rogue" % "1.0.13" intransitive())

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
  ("ssh choosenear.me ln -s " + targetName + " " + "/home/www/builds/root.jar") ! s.log
}

logLevel in push := Level.Debug
