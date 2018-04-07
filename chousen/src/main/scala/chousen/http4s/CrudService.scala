package chousen.http4s

import java.util.UUID

import cats.effect.IO
import chousen.api.core.GameAccess
import chousen.api.data._
import chousen.game.core.GameStateCreation
import chousen.game.status.StatusCalculator
import io.circe.syntax._
import io.circe.{Encoder, Json, Printer}
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.circe.jsonEncoderWithPrinter
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpService, Response}

class CrudService(pbga: GameAccess[IO, Response[IO]], creator: GameStateCreation, sc: StatusCalculator) extends ChousenCookie {

  object NameMatcher extends QueryParamDecoderMatcher[String]("name")

  val routes: HttpService[IO] = {
    import io.circe.generic.extras.semiauto.deriveEnumerationEncoder

    implicit def jsonEnc: EntityEncoder[IO, Json] = jsonEncoderWithPrinter(Printer.noSpaces.copy(dropNullValues = true))
    implicit def statusEncoder: Encoder[StatusEffect] = deriveEnumerationEncoder[StatusEffect]
    import io.circe.generic.auto._

    HttpService {

      // load
      case req@GET -> Root / "game" / id =>

        val optToken = req.requestToken

        val uuid = UUID.fromString(id)

        pbga.withGame(uuid, optToken) { game =>
          val ng = game.copy(player = sc.calculate(game.player))
          val resp = ng.asResponse
          //Access-Control-Allow-Origin: *
          Ok(resp.asJson)
        }

      //  create
      case req@POST -> Root / "game" / playerName / "start" => // used
        val startedGame = creator.createAndStart(playerName)

        val optToken = req.requestToken

        for {
          game <- pbga.storeGame(startedGame, optToken)
          resp = game.asResponse
          asJson: Json = resp.asJson
          result <- Created(asJson)
        } yield result

      //  create
      case req@POST -> Root / "game" / playerName / "start" / IntVar(choice) => // used
        val startedGame = creator.createAndStart(playerName, choice)
        val optToken = req.requestToken

        for {
          game <- pbga.storeGame(startedGame, optToken)
          resp = game.asResponse
          asJson: Json = resp.asJson
          result <- Created(asJson)
        } yield result
    }
  }

}
