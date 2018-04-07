package chousen.http4s

import cats.data.OptionT
import cats.effect.IO
import chousen.api.core.{GameAccess, Http4sMappedGameAccess}
import chousen.game.core.RandomGameStateCreator
import chousen.game.dungeon.{DungeonBuilder, SimpleDungeonBuilder}
import chousen.game.status.StatusCalculator
import org.http4s.{Method, Request, Response, Uri}
import org.http4s.dsl.io.NotFound
import org.scalatest.WordSpec

class CrudServiceSpec extends WordSpec {

  val notFound: Response[IO] = Response[IO](status = NotFound)

  "CrudService" when {

    val dungeonBuilder: DungeonBuilder = new SimpleDungeonBuilder()
    val gameCreator = new RandomGameStateCreator(dungeonBuilder)

    val bobby = gameCreator.create("Bobby")
    val gameMap = Map(bobby.uuid -> bobby)
    val gameAccess: GameAccess[IO, Response[IO]] = new Http4sMappedGameAccess(gameMap)

    val sc = new StatusCalculator
    val service = new CrudService(gameAccess, gameCreator, sc)


    "Creating a game" should {

      val callService: Request[IO] => OptionT[IO, Response[IO]] = service.routes.apply(_: Request[IO])
      val req: Request[IO] = Request[IO](method = Method.POST, uri = Uri.unsafeFromString("/game/david/start"))
      val task: OptionT[IO, Response[IO]] = callService(req)

      lazy val result: Response[IO] = task.value.unsafeRunSync().getOrElse(notFound)

      "Return successfully" in {
        assert(result.status.responseClass.isSuccess)
      }

      "Return with a status of Created" in {
        assert(result.status.code == 201)
      }

    }

    "Creating a game with a choice" should {

      val callService: Request[IO] => OptionT[IO, Response[IO]] = service.routes.apply(_: Request[IO])
      val req: Request[IO] = Request(method = Method.POST, uri = Uri.unsafeFromString("/game/david/start/1"))
      val task = callService(req)

      lazy val result = task.value.unsafeRunSync().getOrElse(notFound)

      "Return successfully" in {
        assert(result.status.responseClass.isSuccess)
      }

      "Return with a status of Created" in {
        assert(result.status.code == 201)
      }

    }


    "Loading a game that does not exist" should {

      val callService: Request[IO] => OptionT[IO, Response[IO]] = service.routes.apply(_: Request[IO])
      val req: Request[IO] = Request[IO](method = Method.GET, uri =
        Uri.unsafeFromString("/game/33673169-266e-417e-a78d-55ba0e2b493c"))
      val task  = callService(req)

      lazy val result: Response[IO] = task.value.unsafeRunSync().getOrElse(notFound)

      "Return a 404" in {
        assert(!result.status.responseClass.isSuccess)
        assert(result.status.code == 404)
      }
    }

    "Loading a game that exists" should {

      val callService: Request[IO] => OptionT[IO, Response[IO]] = service.routes.apply(_: Request[IO])
      val req: Request[IO] = Request[IO](method = Method.GET, uri =
        Uri.unsafeFromString(s"/game/${bobby.uuid}"))
      val task: OptionT[IO, Response[IO]] = callService(req)

      lazy val result: Response[IO] = task.value.unsafeRunSync().getOrElse(notFound)

      "Return a 200 if the game does not exist" in {
        assert(result.status.responseClass.isSuccess)
        assert(result.status.code == 200)
      }
    }
  }
}
