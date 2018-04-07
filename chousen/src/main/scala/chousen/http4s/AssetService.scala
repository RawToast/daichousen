package chousen.http4s

import cats.effect.IO
import org.http4s.dsl.io._
import org.http4s.{HttpService, Request, Response, StaticFile}

class AssetService {
  private def static(file: String, request: Request[IO]): IO[Response[IO]] =
    StaticFile.fromResource("/" + file, Some(request)).getOrElseF(NotFound())

  val routes: HttpService[IO] = HttpService {
    case request @ GET -> Root / "assets" / path if List(".js", ".css").exists(path.endsWith) =>
      static(path, request)
  }
}
