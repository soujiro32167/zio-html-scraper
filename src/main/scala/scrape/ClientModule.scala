package scrape

import org.http4s.{EntityDecoder, Uri}
import org.http4s.client.Client
import zio.{Has, Task, ZIO}

//@accessible
object ClientModule {
  type ClientModule = Has[Client[Task]]

  trait Service extends org.http4s.client.Client[Task]

  def expect[T](uri: Uri)(implicit dec: EntityDecoder[Task, T]) = ZIO.accessM[ClientModule](_.get.expect(uri))
}
