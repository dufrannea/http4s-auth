import cats.effect.IO
import fs2._
import cats._
import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import org.http4s.server.blaze._
import org.http4s.server.middleware.authentication.BasicAuth

import scala.concurrent.ExecutionContext.Implicits.global
import tsec.authentication._
import tsec.common.SecureRandomId

import concurrent.duration._

case class User(name: String, id: Int)

object LocalAuth {
  val bearerTokenStore =
    ExampleAuthHelpers.dummyBackingStore[IO, SecureRandomId, TSecBearerToken[Int]](s => SecureRandomId.coerce(s.id))
  private val userStore: BackingStore[IO, Int, User] = ExampleAuthHelpers.dummyBackingStore[IO, Int, User](_.id)
  private val settings: TSecTokenSettings = TSecTokenSettings(
    expiryDuration = 10.minutes,
    maxIdle = None
  )

  val didier = User("didier", 42)
  userStore.put(didier)

  private val bearerTokenAuth =
    BearerTokenAuthenticator(
      bearerTokenStore,
      userStore,
      settings)

  val Auth =
    SecuredRequestHandler(bearerTokenAuth)
}

object Main extends App with Http4sDsl[IO] {

  private val port = 8080

  private val authedApi: HttpService[IO] = LocalAuth.Auth {
    case request@GET -> Root asAuthed user =>
      Ok(s"hello $user")
  }

  def authenticate(a: BasicCredentials): IO[Option[Int]] = IO {
    if (a.username == "didier")
      Some(42)
    else
      None
  }

  val basicAuth: AuthMiddleware[IO, Int] = BasicAuth("datadoc-datadoc", authenticate)

  private val authEndpoint = AuthedService[Int, IO] {
    case request@GET -> Root / "token" as userId => {

      val token = SecureRandomId.coerce("lol")
      val didierBearer = TSecBearerToken(token, 42, java.time.Instant.MAX, None)
      LocalAuth.bearerTokenStore.put(didierBearer)

      println(s"Adding bearer ${token.toString} (${LocalAuth.bearerTokenStore.count})")
      Ok(token.toString)
    }
  }
  private val tokenEndpoint: HttpService[IO] = basicAuth(authEndpoint)

  new StreamApp[IO]() {
    override def stream(
                         args: List[String],
                         requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] =
      BlazeBuilder[IO].bindHttp(port, "localhost")
        .mountService(authedApi, "/api")
        .mountService(tokenEndpoint, "/user")
        .serve
  }.main(new Array[String](0))
}
