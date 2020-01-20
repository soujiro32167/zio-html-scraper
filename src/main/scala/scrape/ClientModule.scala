package scrape

import org.http4s.client.Client
import zio.{Task, URIO, ZIO}

trait ClientModule extends Serializable{
  val clientModule: ClientModule.Service[Any]
}

object ClientModule {
  trait Service[R] extends Serializable {
    val client: Client[Task]
  }

  object Service {
    val client: URIO[ClientModule, Client[Task]] = ZIO.access[ClientModule](_.clientModule.client)
  }

  def fromClient(c: Client[Task]): ClientModule =  new ClientModule {
    val clientModule: Service[Any] = new Service[Any] {
      val client: Client[Task] = c
    }
  }
}