package scrape

import org.jsoup.nodes.{Document, Element}
import zio.Task

import scala.jdk.CollectionConverters._

object jsoup {
  implicit class ElementOps(element: Element) {
    def zelectFirst(selector: String): Task[Option[Element]] = Task(element.selectFirst(selector)).map(Option(_))
    def zelect(selector: String): Task[List[Element]] = Task(element.select(selector).asScala.toList)
  }

  implicit class DocumentOps(doc: Document) {
    def zelectFirst(selector: String): Task[Option[Element]] = Task(doc.selectFirst(selector)).map(Option(_))
    def zelect(selector: String): Task[List[Element]] = Task(doc.select(selector).asScala.toList)
  }

}
