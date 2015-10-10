name := "mailgun-scala"

organization := "com.roundeights"

version := "0.2"

scalaVersion := "2.11.7"

// Compiler flags
scalacOptions ++= Seq("-deprecation", "-feature")

// Repositories in which to find dependencies
resolvers ++= Seq("RoundEights" at "http://maven.spikemark.net/roundeights")

publishTo := Some("Spikemark" at "https://spikemark.herokuapp.com/maven/roundeights")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// Application dependencies
libraryDependencies ++= Seq(
    "com.roundeights" %% "scalon" % "0.2",
    "com.ning" % "async-http-client" % "1.9+"
)

