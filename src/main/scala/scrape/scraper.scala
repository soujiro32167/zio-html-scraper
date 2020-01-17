package scrape

import org.http4s.Uri
import org.http4s.client.Client
import org.jsoup.Jsoup
import scrape.jsoup._
import zio.interop.catz._
import zio.{RIO, Task, ZIO}

import scala.jdk.CollectionConverters._


object scraper {
  case class Company(name: String, stats: List[(String, String)], reports: List[Report], contact: List[(String, Option[String])])

  case class Report(title: String, year: String, format: String, extent: String, company: String)

  def scrape(uri: Uri): RIO[Client[Task], Company] = for {
    content <- ZIO.accessM[Client[Task]](_.expect[String](uri))
    doc   <- Task(Jsoup.parse(content))
    name  <- doc.zelectFirst(".card-title").map(_.fold("")(_.ownText()))
    stats <- doc.zelect(".list-group-item").map(
      _.map(_.children.asScala.toList.map(_.ownText.replace(":", "").trim).filterNot(_.isEmpty))
        .collect{ case List(k, v) => (k, v) }
    )
    reports <- doc.zelect("#reports-all ~ .row .card").flatMap(es => Task.sequence(es.map( card =>
      for {
        title <- card.zelectFirst("h5").map(_.fold("")(_.text))
        year <- card.zelectFirst(".label-info").map(_.fold("")(_.ownText))
        format <- card.zelectFirst(".label-primary").map(_.fold("")(_.ownText))
        extent <- card.zelectFirst(".label-success").map(_.fold("")(_.ownText))
        company <- card.zelectFirst(".card-text").map(_.fold("")(_.text))
      } yield Report(title, year, format, extent, company)
    )))

    contact <- doc.zelectFirst(".card:has(h4:contains(Contact))").map(_.map( e =>
      e.textNodes.asScala.toList.filterNot(_.isBlank).map(_.text.split(":").map(_.trim)).collect{
        case Array(key, value) => (key, if (value.isEmpty) None else Some(value))
        case Array(key)        => (key, None)
      }
    ).getOrElse(Nil))

  } yield Company(name, stats, reports, contact)
}
