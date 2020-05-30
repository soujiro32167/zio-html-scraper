package scrape

import org.http4s.client.Client
import zio.{Has, Task}

object ClientModule {
  type ClientModule = Has[Client[Task]]
}
