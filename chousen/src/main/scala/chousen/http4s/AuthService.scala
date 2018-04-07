package chousen.http4s

import cats.data.OptionT
import cats.effect.IO
import com.google.api.client.googleapis.auth.oauth2.{GoogleIdToken, GoogleIdTokenVerifier}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.jsonEncoder
import org.http4s.dsl.io._

class AuthService(googleAuthentication: GoogleAuthentication) {

  val routes = HttpService[IO] {
    case req@POST -> Root / "tokensignin" =>

      val reqform: IO[UrlForm] = req.as[UrlForm]

      val result: OptionT[IO, Response[IO]] = for {
        tokenString: String <- OptionT(reqform.map(form => form.getFirst("idtoken")))
        authResponse: AuthResponse <- OptionT(googleAuthentication.authenticateAsync(tokenString))

        jsonResp: Json = authResponse.asJson
        response: Response[IO] <- OptionT.liftF(Ok(jsonResp).map(_.addCookie("chousen", authResponse.userId.getOrElse(tokenString))))
      } yield response

      result.getOrElseF(BadRequest("Unable to complete Google Auth"))
  }
}

class GoogleAuthentication(verifier: GoogleIdTokenVerifier) {


  def authenticate(idToken: String): Option[AuthResponse] = {
    for {
      token: GoogleIdToken <- Option(verifier.verify(idToken))
      payload: GoogleIdToken.Payload <- Option(token.getPayload)
      _ = payload.getEmailVerified
      userId = payload.getSubject
    } yield AuthResponse.create(userId, null)
  }

  def authenticateAsync(idToken: String): IO[Option[AuthResponse]] =
    IO(authenticate(idToken))
}

case class AuthResponse(userId: Option[String], name: Option[String])

object AuthResponse{
  def create(userId: String, name: String) = AuthResponse(Option(userId), Option(name))
}
