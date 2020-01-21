package scrape

import org.http4s.client.Client
import org.http4s.{HttpApp, Request, Response, Status, Uri}
import scrape.scraper.Company
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}

object ScraperSpec extends DefaultRunnableSpec(
  suite("scraper")(
    testM("can scrape") {
      assertM(scraper.scrape(Uri.unsafeFromString("zxc")), hasField[Company, String]("name", _.name, equalTo("A Company")))
    },

    testM("fails on page not found"){
      assertM(scraper.scrape(Uri.unsafeFromString("404")).run, fails(anything))
    }
  ).provideManagedShared{
    val html =
      """
        |<div class="card-title">A Company</div>
        |""".stripMargin
    val htmlServer = HttpApp[Task] {
      case Request(_, Uri(_, _, "404", _, _), _, _, _, _) => Task.effectTotal(Response[Task](Status.NotFound))
      case _ => Task.effectTotal(Response[Task](Status.Ok).withEntity(html))
    }

    ZManaged.succeed(ClientModule.fromClient(Client.fromHttpApp(htmlServer)))
  }
)
