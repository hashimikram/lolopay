import com.typesafe.sbt.packager.MappingsHelper._

name := """lolopay"""
organization := "ro.iss"
maintainer := "zgardancornel@gmail.com"	
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.2"

//activate default play used libraries
libraryDependencies ++= Seq(
  cacheApi,
  ehcache,
  jcache,
  guice,
  ws
)

mappings in Universal ++= directory(baseDirectory.value / "scripts")

//iss queue logger
libraryDependencies += "ro.iss" % "logger" % "0.1.5"

//iss queue email
libraryDependencies += "ro.iss" % "emailsender" % "0.1.1"

//additional play cache
libraryDependencies += "org.jsr107.ri" % "cache-annotations-ri-guice" % "1.0.0"

//import play json
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.1"

//import play iteratees
libraryDependencies += "com.typesafe.play" %% "play-iteratees" % "2.6.1"
libraryDependencies += "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1"

// https://mvnrepository.com/artifact/org.mongodb.morphia/morphia
libraryDependencies += "org.mongodb.morphia" % "morphia" % "1.3.1"

// https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver
libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.4.1"

// https://mvnrepository.com/artifact/org.mindrot/jbcrypt
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

// https://mvnrepository.com/artifact/org.iban4j/iban4j
libraryDependencies += "org.iban4j" % "iban4j" % "3.2.1"

// https://mvnrepository.com/artifact/commons-validator/commons-validator
libraryDependencies += "commons-validator" % "commons-validator" % "1.6"

// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.5"

// https://mvnrepository.com/artifact/org.apache.commons/commons-csv
libraryDependencies += "org.apache.commons" % "commons-csv" % "1.4"

//MangoPay libraries
// libraryDependencies += "com.mangopay" % "mangopay2-java-sdk" % "2.8.0"
// https://mvnrepository.com/artifact/com.google.code.gson/gson
libraryDependencies += "com.google.code.gson" % "gson" % "2.6.2"

// https://mvnrepository.com/artifact/com.opencsv/opencsv
libraryDependencies += "com.opencsv" % "opencsv" % "4.1"

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java           // Java project. Don't expect Scala IDE
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes

// Suppresses problems with Scaladoc @throws links
scalacOptions in (Compile, doc) ++= Seq("-no-link-warnings")

// https://mvnrepository.com/artifact/org.mockito/mockito-core
libraryDependencies += "org.mockito" % "mockito-core" % "2.18.3" % Test

// https://mvnrepository.com/artifact/com.mashape.unirest/unirest-java
libraryDependencies += "com.mashape.unirest" % "unirest-java" % "1.4.9"

// https://mvnrepository.com/artifact/com.googlecode.libphonenumber/libphonenumber
libraryDependencies += "com.googlecode.libphonenumber" % "libphonenumber" % "8.10.13"

