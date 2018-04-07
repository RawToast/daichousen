package chousen.game.actions

import chousen.api.data._
import chousen.game.core.RandomGameStateCreator
import chousen.game.dungeon.SimpleDungeonBuilder
import chousen.game.status.StatusCalculator
import org.scalatest.WordSpec

class SelfActionHandlerSpec extends WordSpec {

  "Self Targeting Action Handler" when {

    val sc = new StatusCalculator
    val selfActionHandler = new SelfActionHandler(sc)

    "Given a self targeting action" should {
      val gameState = GameStateGenerator.gameStateWithFastPlayer

      val dungeonBuilder = new SimpleDungeonBuilder()
      val stateCreator = new RandomGameStateCreator(dungeonBuilder)
      val startedGame: GameState = stateCreator.start(gameState)

      val result = selfActionHandler.handle(HealWounds)(startedGame)

      lazy val numberOfNewMessages = result.messages.size - startedGame.messages.size
      lazy val latestMessages = result.messages.takeRight(numberOfNewMessages)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
        assert(result.messages.map(_.text).exists(_.contains(s"${GameStateGenerator.playerName} uses Heal Wounds and recovers")))
      }

      "Reduce the player's position" in {
        assert(result.player.position < 100)
      }

      "the enemy does not take a turn" in {
        assert(result.player.stats.currentHp == startedGame.player.stats.currentHp)

        assert(latestMessages.exists(!_.text.contains("Slime attacks Test Player")))
      }
    }

    "Given a Haste action" should {
      val gameState = GameStateGenerator.gameStateWithFastPlayer

      val dungeonBuilder = new SimpleDungeonBuilder()
      val stateCreator = new RandomGameStateCreator(dungeonBuilder)
      val startedGame: GameState = stateCreator.start(gameState)

      val result = selfActionHandler.handle(Haste)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
        assert(result.messages.contains(GameMessage(s"${GameStateGenerator.playerName} uses Haste!")))
      }

      "Reduce the player's position" in {
        assert(result.player.position < 100)
      }

      "The Player gains the Haste status" in {
        assert(result.player.status.nonEmpty)
        assert(result.player.status.exists(_.effect == Fast))
      }
    }

    "Given Potion of Rage" should {
      val gameState = GameStateGenerator.gameStateWithFastPlayer

      val dungeonBuilder = new SimpleDungeonBuilder()
      val stateCreator = new RandomGameStateCreator(dungeonBuilder)
      val startedGame: GameState = stateCreator.start(gameState)

      val result = selfActionHandler.handle(PotionOfRage)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
        assert(result.messages.contains(GameMessage(s"${GameStateGenerator.playerName} drinks a Potion of Rage!")))
      }

      "Reduce the player's position" in {
        assert(result.player.position < 100)
      }

      "The Player gains the Berserk status" in {
        assert(result.player.status.nonEmpty)
        assert(result.player.status.exists(_.effect == Rage))
      }
    }

    "Given an Essence of Vitality" should {
      val gameState = GameStateGenerator.gameStateWithFastPlayer

      val dungeonBuilder = new SimpleDungeonBuilder()
      val stateCreator = new RandomGameStateCreator(dungeonBuilder)
      val startedGame: GameState = stateCreator.start(gameState)

      val result = selfActionHandler.handle(EssenceOfVitality)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
      }

      "Not affect the player's position" in {
        assert(result.player.position >= 100)
        assert(startedGame.player.position == result.player.position)
      }

      "The Player's vitality increases" in {
        assert(result.player.stats.vitality > startedGame.player.stats.vitality)
      }

      "Set the essence flag" in {
        assert(result.cards.playedEssence)
      }
    }

    "Given an Essence of Strength" should {
      val gameState = GameStateGenerator.gameStateWithFastPlayer

      val dungeonBuilder = new SimpleDungeonBuilder()
      val stateCreator = new RandomGameStateCreator(dungeonBuilder)
      val startedGame: GameState = stateCreator.start(gameState)

      val result = selfActionHandler.handle(EssenceOfStrength)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
      }

      "Not affect the player's position" in {
        assert(result.player.position >= 100)
        assert(startedGame.player.position == result.player.position)
      }

      "The Player's strength increases" in {
        assert(result.player.stats.strength > startedGame.player.stats.strength)
      }

      "Set the essence flag" in {
        assert(result.cards.playedEssence)
      }
    }

    "Given an Essence of Dexterity" should {
      val gameState = GameStateGenerator.gameStateWithFastPlayer

      val dungeonBuilder = new SimpleDungeonBuilder()
      val stateCreator = new RandomGameStateCreator(dungeonBuilder)
      val startedGame: GameState = stateCreator.start(gameState)

      val result = selfActionHandler.handle(EssenceOfDexterity)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
      }

      "Not affect the player's position" in {
        assert(result.player.position >= 100)
        assert(startedGame.player.position == result.player.position)
      }

      "The Player's dexterity increases" in {
        assert(result.player.stats.dexterity > startedGame.player.stats.dexterity)
      }

      "Set the essence flag" in {
        assert(result.cards.playedEssence)
      }
    }

    "Given an Essence of Intelligence" should {
      val gameState = GameStateGenerator.gameStateWithFastPlayer

      val dungeonBuilder = new SimpleDungeonBuilder()
      val stateCreator = new RandomGameStateCreator(dungeonBuilder)
      val startedGame: GameState = stateCreator.start(gameState)

      val result = selfActionHandler.handle(EssenceOfIntelligence)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
      }

      "Not affect the player's position" in {
        assert(result.player.position >= 100)
        assert(startedGame.player.position == result.player.position)
      }

      "The Player's intellect increases" in {
        assert(result.player.stats.intellect > startedGame.player.stats.intellect)
      }

      "Set the essence flag" in {
        assert(result.cards.playedEssence)
      }
    }

  }
}
