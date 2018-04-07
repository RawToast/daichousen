package chousen

import java.util.Collections

import cats.effect.IO
import chousen.api.core.{GameAccess, PlayerBasedGameAccess}
import chousen.api.data.GameState
import chousen.game.actions.DamageCalculator
import chousen.game.core.{GameManager, GameStateManager, RandomGameStateCreator}
import chousen.game.dungeon.{DungeonBuilder, SimpleDungeonBuilder}
import chousen.game.status.{PostTurnStatusCalculator, StatusCalculator}
import chousen.http4s._
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.http4s.Response
import fs2.StreamApp
import scala.concurrent.ExecutionContext.Implicits.global

object Http4sServer extends StreamApp[IO] {

  import org.http4s.server.blaze.BlazeBuilder

  def buildServer: BlazeBuilder[IO] = {

    val port = Option(System.getProperty("http.port")).getOrElse("8080").toInt
    val host = Option(System.getProperty("http.host")).getOrElse("0.0.0.0")

    val dungeonBuilder: DungeonBuilder = new SimpleDungeonBuilder()

    val gameCreator = new RandomGameStateCreator(dungeonBuilder)
    val statusCalculator = new StatusCalculator
    val damageCalculator = new DamageCalculator(statusCalculator)
    val postTurnStatusCalc = new PostTurnStatusCalculator

    val gameStateManager: GameManager[GameState] = new GameStateManager(damageCalculator, postTurnStatusCalc)


    // Note that authentication is not enabled on the frontend
    val apiKey = "<insert_key_here>.apps.googleusercontent.com"
    val googleAuth = new GoogleAuthentication(
      new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport, JacksonFactory.getDefaultInstance)
        .setAudience(Collections.singletonList(apiKey))
        .build())

    val playerBasedGameAccess: GameAccess[IO, Response[IO]] = new PlayerBasedGameAccess()

    val crudService: CrudService = new CrudService(playerBasedGameAccess, gameCreator, statusCalculator)
    val authService = new AuthService(googleAuth)
    val inputService = new InputService(playerBasedGameAccess, gameStateManager, statusCalculator)
    val assetService = new AssetService()
    val executionContext = scala.concurrent.ExecutionContext.global

    BlazeBuilder[IO].bindHttp(port, host)
      .withExecutionContext(executionContext)
      .mountService(crudService.routes)
      .mountService(inputService.routes)
      .mountService(assetService.routes)
      .mountService(authService.routes)
  }


  override def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] =
    buildServer.serve
}
