import sbt._

object ChoosenearMeBuild extends Build {
  val push = TaskKey[Unit]("push", "Pushes code to EC2")
  lazy val project = Project("default", file("."))
}
