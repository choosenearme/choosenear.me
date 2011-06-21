import sbt._

object ChoosenearMeBuild extends Build {
  val push = TaskKey[Unit]("push", "Pushes code to EC2")
  val pushStatic = TaskKey[Unit]("push-static", "Pushes static files to EC2")
  val remoteApiRestart = TaskKey[Unit]("remote-api-restart", "Restarts choosenear.me API process through supervisor")
  lazy val project = Project("default", file("."))
}
