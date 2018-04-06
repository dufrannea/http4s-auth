scalaVersion := "2.12.4"

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"

val http4sVersion = "0.18.7"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion
)
val tsecV = "0.0.1-M6"

resolvers += "jmcardon at bintray" at "https://dl.bintray.com/jmcardon/tsec"
libraryDependencies ++= Seq(
  "io.github.jmcardon" %% "tsec-common" % tsecV,
  "io.github.jmcardon" %% "tsec-password" % tsecV,
  "io.github.jmcardon" %% "tsec-symmetric-cipher" % tsecV,
  "io.github.jmcardon" %% "tsec-mac" % tsecV,
  "io.github.jmcardon" %% "tsec-signatures" % tsecV,
  "io.github.jmcardon" %% "tsec-md" % tsecV,
  "io.github.jmcardon" %% "tsec-jwt-mac" % tsecV,
  "io.github.jmcardon" %% "tsec-jwt-sig" % tsecV,
  "io.github.jmcardon" %% "tsec-http4s" % tsecV
)
