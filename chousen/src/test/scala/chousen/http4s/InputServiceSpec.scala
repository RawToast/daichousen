package chousen.http4s

import java.util.UUID

import cats.data.OptionT
import cats.effect.IO
import chousen.Optics._
import chousen.api.core.{GameAccess, Http4sMappedGameAccess}
import chousen.api.data._
import chousen.game.actions.DamageCalculator
import chousen.game.core.{GameManager, GameStateManager, RandomGameStateCreator}
import chousen.game.dungeon.{DungeonBuilder, SimpleDungeonBuilder}
import chousen.game.status.{PostTurnStatusCalculator, StatusCalculator}
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s.dsl.io._
import org.http4s.circe._
import org.http4s.{Entity, EntityEncoder, Method, Request, Response, Uri}
import org.scalatest.WordSpec

class InputServiceSpec extends WordSpec {

  val notFound = Response[IO](status = NotFound)

  "InputServiceSpec" when {

    val dungeonBuilder: DungeonBuilder = new SimpleDungeonBuilder()
    val gameCreator = new RandomGameStateCreator(dungeonBuilder)

    val chainmailCardId = "12224f23-d5aa-4026-a229-56b881479714"
    val chainmailCard = Card(UUID.fromString(chainmailCardId), "Chainmail",  "test", Ringmail)

    val game = gameCreator.create("Bobby")

    val bobby = HandLens.modify(_ :+ chainmailCard)(game)

    val gameMap = Map(bobby.uuid -> bobby)
    val gameAccess: GameAccess[IO, Response[IO]] = new Http4sMappedGameAccess(gameMap)

    val statusCalculator = new StatusCalculator
    val damageCalculator = new DamageCalculator(statusCalculator)
    val postTurnCalc = new PostTurnStatusCalculator

    val gameStateManager: GameManager[GameState] = new GameStateManager(damageCalculator, postTurnCalc)

    val service = new InputService(gameAccess, gameStateManager, statusCalculator)


    "Handling a Basic Attack request" should {

      val attack = AttackRequest(bobby.dungeon.currentEncounter.enemies.head.id)
      implicit val enc: EntityEncoder[IO, AttackRequest] = jsonEncoderOf[IO, AttackRequest]

      val ent: Entity[IO] = enc.toEntity(attack).unsafeRunSync()
      val callService2: Request[IO] => OptionT[IO, Response[IO]] = service.routes.apply(_: Request[IO])
      val req: Request[IO] = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"/game/${bobby.uuid}/attack"),
        body = ent.body)
      val task: OptionT[IO, Response[IO]] = callService2(req)


      lazy val result: Response[IO] = task.value.unsafeRunSync().getOrElse(notFound)


      "Return successfully" in {
        assert(result.status.responseClass.isSuccess)
      }

      "Return with a status of Ok" in {
        assert(result.status.code == 200)
      }
    }

    "Handling a Block request" should {

      val attack = BlockRequest()
      implicit val enc: EntityEncoder[IO, BlockRequest] = jsonEncoderOf[IO, BlockRequest]

      val ent: Entity[IO] = enc.toEntity(attack).unsafeRunSync()
      val callService: Request[IO] => OptionT[IO, Response[IO]] = service.routes.apply(_: Request[IO])
      val req: Request[IO] = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"/game/${bobby.uuid}/block"),
        body = ent.body)

      lazy val result = callService(req).getOrElse(notFound).unsafeRunSync()

      "Return successfully" in {
        assert(result.status.responseClass.isSuccess)
      }

      "Return with a status of Ok" in {
        assert(result.status.code == 200)
      }
    }

    "Handling an Equipment request" when {
      import io.circe.generic.extras.semiauto.deriveEnumerationEncoder
      implicit val enumDecoder: Encoder[EquipAction] = deriveEnumerationEncoder[EquipAction]
      implicit val enc: EntityEncoder[IO, EquipmentActionRequest] = jsonEncoderOf[IO, EquipmentActionRequest]

      "When given an valid equipment request" should {
        val actionRequest = EquipmentActionRequest(UUID.fromString(chainmailCardId), RedCape)

        val ent: Entity[IO] = enc.toEntity(actionRequest).unsafeRunSync()
        val callService: Request[IO] => OptionT[IO, Response[IO]] = service.routes.apply(_: Request[IO])

        val req: Request[IO] = Request[IO](method = Method.POST,
          uri = Uri.unsafeFromString(s"/game/${bobby.uuid}/equip/$chainmailCardId"),
          body = ent.body)
        val task: IO[Response[IO]] = callService(req).getOrElse(notFound)

        lazy val result: Response[IO] = task.unsafeRunSync()

        "Return successfully" in {
          assert(result.status.responseClass.isSuccess)
        }

        "Return with a status of Ok (200)" in {
          assert(result.status.code == 200)
        }
      }

      "When given an invalid Card ID" should {
        val altId = UUID.randomUUID()
        val actionRequest = EquipmentActionRequest(altId, Ringmail)

        val ent: Entity[IO] = enc.toEntity(actionRequest).unsafeRunSync()
        val callService: Request[IO] => OptionT[IO, Response[IO]] = service.routes.apply(_: Request[IO])

        val req: Request[IO] = Request[IO](method = Method.POST,
          uri = Uri.unsafeFromString(s"/game/${bobby.uuid}/equip/$altId"),
          body = ent.body)

        lazy val result: Response[IO] = callService(req).getOrElse(notFound).unsafeRunSync()

        "Return unsuccessfully" in {
          assert(!result.status.responseClass.isSuccess)
        }

        "Return with a status of NotFound (404)" in {
          assert(result.status.code == 404)
        }
      }
    }

  }
}