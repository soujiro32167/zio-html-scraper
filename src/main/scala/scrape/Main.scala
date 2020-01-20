package scrape
import java.security.SecureRandom
import java.security.cert.X509Certificate

import javax.net.ssl.{SSLContext, X509TrustManager}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.RequestLogger
import org.http4s.implicits._
import zio._
import zio.console._
import zio.interop.catz._
import zio.macros.delegate._
import zio.macros.delegate.syntax._

object Main extends ManagedApp {

  private val log = org.log4s.getLogger

  lazy val TrustingSslContext: SSLContext = {
    val trustManager = new X509TrustManager {
      def getAcceptedIssuers(): Array[X509Certificate] = Array.empty
      def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
      def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
    }
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, Array(trustManager), new SecureRandom)
    sslContext
  }

  private def withRequestLogger(client: Client[Task]): Client[Task] = RequestLogger[Task](logHeaders = false, logBody = true, logAction = Some(s => Task(org.log4s.getLogger(scraper.getClass).debug(s))))(client)

  override def run(args: List[String]): ZManaged[ZEnv, Nothing, Int] = {
    ZIO.runtime[ZEnv].toManaged_.flatMap{ implicit rts =>
      val managed = BlazeClientBuilder[Task](rts.platform.executor.asEC).withSslContext(TrustingSslContext).resource.map(withRequestLogger).toManaged
      ZIO.environment[ZEnv] @@ enrichWithManaged[ClientModule](managed.map(ClientModule.fromClient)) >>> myAppLogic.toManaged_
    }.fold(_ => 1, _ => 0 )
  }

  private val myAppLogic =
    RIO.sequenceParN(3)(
      (10 to 50)
        .map(page =>
          scraper.scrape(uri"https://database.globalreporting.org/organizations" / page.toString / "")
          .timed
          .tapError(e => Task(log.warn(e)(s"Failed on page $page")))
          .either
          .tap(c => putStrLn(c.toString))
      ))
}
