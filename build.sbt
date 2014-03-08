name := "mailgun-scala"

organization := "com.roundeights"

version := "0.1"

scalaVersion := "2.10.3"

// Compiler flags
scalacOptions ++= Seq("-deprecation", "-feature")

// Repositories in which to find dependencies
resolvers ++= Seq("RoundEights" at "http://maven.spikemark.net/roundeights")

publishTo := Some("Spikemark" at "https://spikemark.herokuapp.com/maven/roundeights")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// Application dependencies
libraryDependencies ++= Seq(
    "com.roundeights" %% "scalon" % "0.2",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0"
)

