import BuildHelper._

inThisBuild(
  List(
    developers := List(
      Developer(
        "soujiro32167",
        "Eli Kasik",
        "soujiro32167@gmail.com",
        url("https://github.com/soujiro32167")
      )
    ),
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

val zioVersion = "1.0.0-RC20"
val http4sVersion = "0.21.4"

libraryDependencies ++= Seq(
  "dev.zio"   %% "zio"          % zioVersion,
  "dev.zio" %% "zio-macros" % zioVersion,
  "dev.zio"   %% "zio-interop-cats"    % "2.1.3.0-RC15",
  "dev.zio"   %% "zio-streams" % zioVersion,
  "org.http4s"  %% "http4s-blaze-client" % http4sVersion,
  "org.jsoup" % "jsoup" % "1.12.1",

  "ch.qos.logback"  %  "logback-classic"     % "1.2.3",
  "org.log4s"       %% "log4s" % "1.8.2",
  "dev.zio" %% "zio-test"     % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val root =
  (project in file("."))
    .settings(
      stdSettings("miron-scrape"),
      scalacOptions += "-Ymacro-annotations"
    )
    .settings(buildInfoSettings("scrape"))
    .enablePlugins(BuildInfoPlugin)

lazy val docs = project
  .in(file("miron-scrape-docs"))
  .settings(
    skip.in(publish) := true,
    moduleName := "miron-scrape-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion
    ),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(root),
    target in (ScalaUnidoc, unidoc) := (baseDirectory in LocalRootProject).value / "website" / "static" / "api",
    cleanFiles += (target in (ScalaUnidoc, unidoc)).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(unidoc in Compile).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(unidoc in Compile).value
  )
  .dependsOn(root)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
