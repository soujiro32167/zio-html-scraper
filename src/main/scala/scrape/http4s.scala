package scrape

import org.http4s.Response
import zio.Task
import zio.interop.catz._

object http4s {
  implicit final class ResponseOps(res: Response[Task]) {
    def asText: Task[String] = res.bodyAsText.compile.string
  }
}
