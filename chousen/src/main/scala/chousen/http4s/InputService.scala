package chousen.http4s

import java.util.UUID

import cats.effect._
import chousen.api.core.GameAccess
import chousen.api.data._
import chousen.game.core.GameManager
import chousen.game.status.StatusCalculator
import chousen.util.GameResponse
import io.circe.generic.auto._
import io.circe.generic.extras.semiauto.deriveEnumerationEncoder
import io.circe.syntax._
import io.circe._
import org.http4s.circe._
import org.http4s.circe.jsonEncoderWithPrinter
import org.http4s.dsl.io._
import org.http4s._

class InputService(ga: GameAccess[IO, Response[IO]], gsm: GameManager[GameState], sc: StatusCalculator) extends ChousenCookie {

  implicit val decoderJ: EntityDecoder[IO, Json] = jsonOf[IO, Json]

  private def getIds(uuid: String, cardUuid: String) =
    (UUID.fromString(uuid), UUID.fromString(cardUuid))

  implicit def jsonEnc = jsonEncoderWithPrinter(Printer.noSpaces.copy(dropNullValues = true))

  implicit def statusEncoder: Encoder[StatusEffect] = deriveEnumerationEncoder[StatusEffect]

  val routes: HttpService[IO] = {
    import io.circe.generic.extras.semiauto._

    HttpService {
      // Attacks
      case req@POST -> Root / "game" / uuid / "attack" =>
        val id = UUID.fromString(uuid)

        val optToken = req.requestToken

        ga.withGame(id, optToken) { g =>
          basicRequest[AttackRequest](req, g)(gsm.takeCommand)
        }

      case req@POST -> Root / "game" / uuid / "block" =>
        val id = UUID.fromString(uuid)
        val optToken = req.requestToken

        ga.withGame(id, optToken) { g =>
          basicRequest[BlockRequest](req, g)(gsm.takeCommand)
        }

      case req@POST -> Root / "game" / uuid / "single" / cardUuid =>
        implicit val enumDecoder: Decoder[SingleTargetAction] = deriveEnumerationDecoder[SingleTargetAction]

        val (id, cardId) = getIds(uuid, cardUuid)
        val optToken = req.requestToken

        ga.withGame(id, optToken) { g =>
          cardRequest[SingleTargetActionRequest](req, g, cardId)(gsm.useCard)
        }

      case req@POST -> Root / "game" / uuid / "self" / cardUuid =>
        implicit val enumDecoder: Decoder[SelfAction] = deriveEnumerationDecoder[SelfAction]

        val (id, cardId) = getIds(uuid, cardUuid)

        val optToken = req.requestToken

        ga.withGame(id, optToken) { g =>
          cardRequest[SelfInflictingActionRequest](req, g, cardId)(gsm.useCard)
        }

      case req@POST -> Root / "game" / uuid / "card" / cardUuid =>
        implicit val enumDecoder: Decoder[CardAction] = deriveEnumerationDecoder[CardAction]

        val (id, cardId) = getIds(uuid, cardUuid)

        val optToken = req.requestToken

        ga.withGame(id, optToken) { g =>
          cardRequest[CardActionRequest](req, g, cardId)(gsm.useCard)
        }

      case req@POST -> Root / "game" / uuid / "multi" / cardUuid =>
        implicit val enumDecoder: Decoder[MultiAction] = deriveEnumerationDecoder[MultiAction]

        val (id, cardId) = getIds(uuid, cardUuid)

        val optToken = req.requestToken

        ga.withGame(id, optToken) { g =>
          cardRequest[MultiTargetActionRequest](req, g, cardId)(gsm.useCard)
        }

      case req@POST -> Root / "game" / uuid / "camp" / cardUuid =>
        implicit val enumDecoder: Decoder[CampFireAction] = deriveEnumerationDecoder[CampFireAction]

        val (id, cardId) = getIds(uuid, cardUuid)

        val optToken = req.requestToken

        val resp = ga.withGame(id, optToken) { g =>
          passiveRequest[CampfireActionRequest](req, g, cardId)(gsm.useCard)
        }

        resp

      case req@POST -> Root / "game" / uuid / "equip" / cardUuid =>
        implicit val enumDecoder: Decoder[EquipAction] = deriveEnumerationDecoder[EquipAction]

        val (id, cardId) = getIds(uuid, cardUuid)

        val optToken = req.requestToken

        ga.withGame(id, optToken) { g =>
          cardRequest[EquipmentActionRequest](req, g, cardId)(gsm.useCard)
        }
    }
  }

  private def cardRequest[T <: CommandRequest](req: Request[IO], g: GameState, cardId: UUID)
                                              (f: (Card, CommandRequest, GameState) => GameState)
                                              (implicit decoder: Decoder[T]): IO[Response[IO]] = {

    val optCard = g.cards.hand
                  .find(_.id == cardId)
                  .fold(g.cards.equippedCards.skills.find(_.id == cardId))((c: Card) => Option(c))
    implicit val ioDecoder: EntityDecoder[IO, T] = jsonOf[IO, T]

    optCard match {
      case Some(card) => for {
        ar <- req.as[T]
        ng = f(card, ar, g)
        _ <- ga.storeGame(ng, req.requestToken)
        game = ng.copy(player = sc.calculate(ng.player))
        resp = game.asResponse(g.messages)
        res <- Ok.apply(resp.asJson)
      } yield res
      case None => NotFound(g.asJson)
    }
  }

  private def passiveRequest[T <: CommandRequest](req: Request[IO], g: GameState, cardId: UUID)
                                                 (f: (Card, T, GameState) => GameState)
                                                 (implicit decoder: Decoder[T]): IO[Response[IO]] = {
    implicit val ioDecoder: EntityDecoder[IO, T] = jsonOf[IO, T]

    g.cards.passive.find(_.id == cardId) match {
      case Some(card) => for {
        ar <- req.as[T]
        ng = f(card, ar, g)
        _ <- ga.storeGame(ng, req.requestToken)
        game = ng.copy(player = sc.calculate(ng.player))
        resp: GameResponse = game.asResponse(g.messages)
        res <- Ok.apply(resp.asJson)
      } yield res
      case None => NotFound(g.asJson)
    }
  }

  private def basicRequest[T <: CommandRequest](req: Request[IO], g: GameState)
                                               (f: (T, GameState) => GameState)
                                               (implicit decoder: Decoder[T]): IO[Response[IO]] = {
    implicit val ioDecoder: EntityDecoder[IO, T] = jsonOf[IO, T]

    for {
      ar <- req.as[T]
      ng = f(ar, g)
      _ <- ga.storeGame(ng, req.requestToken)
      game = ng.copy(player = sc.calculate(ng.player))
      resp: GameResponse = game.asResponse(g.messages)
      res <- Ok.apply(resp.asJson)
    } yield res
  }

}
