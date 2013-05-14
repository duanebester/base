name := "base"

version := "0.0.1"

organization := "com.besterdesigns"

scalaVersion := "2.10.0"

resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "http://oss.sonatype.org/content/repositories/releases",
                "Default maven repository" at "http://repo1.maven.org/maven2/",
  				"Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  				"Scala Tools Releases" at "http://scala-tools.org/repo-releases/",
  				"Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/",
  				"Scales Repo" at "http://scala-scales.googlecode.com/svn/repo",
  				"OpenNMS - for fast-md5" at "http://repo.opennms.org/maven2/"
                )

seq(com.github.siasia.WebPlugin.webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.5-RC5"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-mapper"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-record"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-squeryl-record" %  liftVersion exclude("org.squeryl", "squeryl"),
    "org.squeryl"       %% "squeryl" % "0.9.5",
    "net.liftmodules"   %% "lift-jquery-module_2.5" % "2.3",
    "org.eclipse.jetty" % "jetty-webapp"        % "8.1.7.v20120910"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    % "logback-classic"     % "1.0.6",
    "org.specs2"        %% "specs2"             % "1.14"           % "test",
    "com.h2database"    % "h2"                  % "1.3.167",
    "mysql"             % "mysql-connector-java" % "5.1.24",
    "com.jolbox"        % "bonecp"               % "0.8.0-rc1"
  )
}

