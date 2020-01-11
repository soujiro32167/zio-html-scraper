import BuildHelper._

inThisBuild(
  List(
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "soujiro32167",
        "Eli Kasik",
        "soujiro32167@gmail.com",
        url("https://github.com/soujiro32167")
      )
    ),
//    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
//    pgpPublicRing := file("/tmp/public.asc"),
//    pgpSecretRing := file("/tmp/secret.asc"),
//    scmInfo := Some(
//      ScmInfo(url("https://github.com/zio/miron-scrape/"), "scm:git:git@github.com:zio/miron-scrape.git")
//    )
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

val zioVersion = "1.0.0-RC17"
libraryDependencies ++= Seq(
  "dev.zio" %% "zio"          % zioVersion,

  "org.jsoup" % "jsoup" % "1.12.1",

  "dev.zio" %% "zio-test"     % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val root =
  (project in file("."))
    .settings(
      stdSettings("miron-scrape")
    )
    .settings(buildInfoSettings("miron-scrape"))
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
