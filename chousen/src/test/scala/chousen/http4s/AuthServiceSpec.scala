package chousen.http4s

import cats.effect.IO
import org.http4s.{Entity, EntityEncoder, Method, Request, Response, Uri, UrlForm}
import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.http4s.dsl.io._
import org.scalatest.mockito.MockitoSugar


class AuthServiceSpec extends WordSpec with MockitoSugar {

  "Auth Service" when {

    val happyMock: GoogleAuthentication = mock[GoogleAuthentication]
    val happyService = new AuthService(happyMock)
    val testToken = "testtoken"

    val form: UrlForm = UrlForm.apply(Map("idtoken" -> Seq(testToken)))
    val enc: EntityEncoder[IO, UrlForm] = UrlForm.entityEncoder
    val ent: Entity[IO] = enc.toEntity(form).unsafeRunSync()
    val req: Request[IO] = Request(method = Method.POST, uri = Uri.unsafeFromString(s"/tokensignin"),
      body = ent.body)


    "Authenticator returns successfully" should {

      "Return success with cookie" in {

        when(happyMock.authenticateAsync(testToken)).thenReturn(IO(Option(AuthResponse(Some("testId"), Some("2")))))

        val result = happyService.routes.apply(req).value.unsafeRunSync().getOrElse(Response[IO](status = NotFound))

        assert(result.status.code == 200)
        assert(result.cookies.nonEmpty)
      }
    }

    "Authenticator returns nothing" should {

      "Return failure" in {
        when(happyMock.authenticateAsync(testToken)).thenReturn(IO(None))

        val result = happyService.routes.apply(req).value.unsafeRunSync().getOrElse(Response[IO](status = NotFound))

        assert(result.status.code == 400)
        assert(result.cookies.isEmpty)
      }
    }
  }

}
