package chousen.api.core

import java.util.UUID
import cats.effect.IO
import chousen.api.data._
import org.http4s.Response

trait GameAccess[T[_], R] {
  def withGame(id: UUID, playerId: Option[String] = None)(f: GameState => T[R]): T[R]

  def storeGame(g: GameState, playerId: Option[String] = None): T[GameState]
}

class Http4sMappedGameAccess(private var store: Map[UUID, GameState] = Map.empty) extends GameAccess[IO, Response[IO]] {

  import io.circe.generic.auto._
  import io.circe.syntax._
  import org.http4s.circe._
  import org.http4s.dsl.io._

  def withGame(id: UUID, playerId: Option[String] = None)(f: GameState => IO[Response[IO]]): IO[Response[IO]] = {

    case class Error(msg: String)

    store.get(id) match {
      case Some(game) => f(game)
      case None => NotFound(Error(s"Game with ID=$id does not exist").asJson)
    }
  }

  override def storeGame(g: GameState, playerId: Option[String] = None): IO[GameState] = {
    store = store + (g.uuid -> g)
    IO(g)
  }
}

class PlayerBasedGameAccess(private var store: Map[String, Map[UUID, GameState]] = Map.empty) extends GameAccess[IO, Response[IO]] {

  import io.circe.generic.auto._
  import io.circe.syntax._
  import org.http4s.circe._
  import org.http4s.dsl.io._

  def withGame(id: UUID, playerId: Option[String])(f: GameState => IO[Response[IO]]): IO[Response[IO]] = {

    case class Error(msg: String)

    val pid = playerId.getOrElse("test")

    store.get(pid)
    .flatMap(_.get(id))
    .fold(NotFound(Error(s"Game with ID=$id does not exist").asJson))(f(_))
  }

  def storeGame(g: GameState, playerId: Option[String]): IO[GameState] = {
    val pid = playerId.getOrElse("test")

    val gs: Map[UUID, GameState] = store.get(pid)
                                   .fold(Map[UUID, GameState](g.uuid -> g))(st => st + (g.uuid -> g))

    store = store + (pid -> gs)

    IO(g)
  }

}