package chousen.game.actions

import chousen.api.data._
import chousen.game.core.RandomGameStateCreator
import chousen.game.dungeon.SimpleDungeonBuilder
import org.scalatest.WordSpec

class CardActionHandlerSpec extends WordSpec {

  "Card Action Handler" when {

    val gameState = GameStateGenerator.gameStateWithFastPlayer

    val dungeonBuilder = new SimpleDungeonBuilder()
    val stateCreator = new RandomGameStateCreator(dungeonBuilder)

    "Given a card action" should {
      val startedGame: GameState = stateCreator.start(gameState)

      val result = CardActionHandler.handle(Rummage, None)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
      }

    }

    "Given Rummage" should {
      val startedGame: GameState = stateCreator.start(gameState)

      val result = CardActionHandler.handle(Rummage, None)(startedGame)

      "Draw two cards" in {
        assert(result.cards.hand.size > (1 + startedGame.cards.hand.size))
      }
    }

    "Given BagOfGold" should {
      val startedGame: GameState = stateCreator.start(gameState)

      val result = CardActionHandler.handle(BagOfGold, None)(startedGame)

      "Give the player 30 gold" in {
        assert(result.player.gold > startedGame.player.gold)
        val diff = result.player.gold - startedGame.player.gold
        assert(diff == 30)
      }
    }
  }
}
