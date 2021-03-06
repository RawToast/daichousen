package chousen.game.actions

import java.util.UUID

import chousen.api.data._
import chousen.game.core.RandomGameStateCreator
import chousen.game.dungeon.SimpleDungeonBuilder
import org.scalatest.WordSpec

class EquipmentActionHandlerSpec extends WordSpec {

  "Equipment Action Handler" when {

    // val sc = new StatusCalculator
    val equipActionHandler = new EquipmentActionHandler()
    lazy val uuid = UUID.randomUUID()

    val gameState = GameStateGenerator.gameStateWithFastPlayer

    val dungeonBuilder = new SimpleDungeonBuilder()
    val stateCreator = new RandomGameStateCreator(dungeonBuilder)
    val startedGame: GameState = stateCreator.start(gameState)

    "Given an equipment action" should {

      val result = equipActionHandler.handle(Club, uuid)(startedGame)

      lazy val numberOfNewMessages = result.messages.size - startedGame.messages.size
      lazy val latestMessages = result.messages.takeRight(numberOfNewMessages)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
        assert(result.messages.contains(GameMessage(s"${GameStateGenerator.playerName} equips Club.")))
      }

      "Reduce the player's position" in {
        assert(result.player.position < 100)
      }

      "Equips the item" in {
        assert(startedGame.player.equipment.weapon.isEmpty)
        assert(result.player.equipment.weapon.nonEmpty)
      }

      "the enemy does not take a turn" in {
        assert(result.player.stats.currentHp == startedGame.player.stats.currentHp)

        assert(latestMessages.exists(!_.text.contains("Slime ")))
      }

      //      "the equivalent card is placed into the passive deck" in {
      //        assert(result.cards.passive.size > startedGame.cards.passive.size)
      //      }
    }

    "Given a weapon EquipAction when the player is already equipped" should {

      import chousen.Optics._
      val gameWithWeapon = PlayerLens.composeLens(PlayerWeaponLens).set(Option(Weapon(UUID.randomUUID(), "Test", 10)))(startedGame)

      val result = equipActionHandler.handle(TrollCrusher, uuid)(gameWithWeapon)

      "State the action was used" in {
        assert(result.messages.size > gameWithWeapon.messages.size)
      }

      "Reduce the player's position" in {
        assert(result.player.position < 100)
      }

      "Equips the item" in {
        assert(result.player.equipment.weapon.nonEmpty)
        assert(gameWithWeapon.player.equipment.weapon.nonEmpty)
        assert(result.player.equipment.weapon != gameWithWeapon.player.equipment.weapon)
      }
    }

    "Given a EquipAction that is of the armour type" should {

      val result = equipActionHandler.handle(Ringmail, uuid)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
      }

      "Reduce the player's position" in {
        assert(result.player.position < 100)
      }

      "Equips the item as armour" in {
        assert(result.player.equipment.armour.nonEmpty)
        assert(result.player.equipment.weapon.isEmpty)
        assert(result.player.equipment.armour != startedGame.player.equipment.armour)
      }
    }

    "Given an armour type EquipAction when the player has a weapon equipped" should {

      val weaponEquippedGame = equipActionHandler.handle(RenartsDeceiver, uuid)(startedGame)
      val result = equipActionHandler.handle(RedCape, uuid)(weaponEquippedGame)

      "State the action was used" in {
        assert(result.messages.size > weaponEquippedGame.messages.size)
      }

      "Reduce the player's position" in {
        assert(result.player.position < 100)
      }

      "Equips the item as armour" in {
        assert(result.player.equipment.armour.nonEmpty)

        assert(weaponEquippedGame.player.equipment.weapon.nonEmpty)
        assert(weaponEquippedGame.player.equipment.armour.isEmpty)

        assert(result.player.equipment.armour != startedGame.player.equipment.armour)
      }

      "Does not affect the equipped weapon" in {
        assert(weaponEquippedGame.player.equipment.weapon.nonEmpty)
        assert(result.player.equipment.weapon.nonEmpty)
        assert(result.player.equipment.weapon == weaponEquippedGame.player.equipment.weapon)
      }
    }

    "Given Club" should {
      val result = equipActionHandler.handle(Club, uuid)(startedGame)

      equipWeaponAssertions(result, startedGame)
    }

    "Given TrollCrusher" should {
      val result = equipActionHandler.handle(TrollCrusher, uuid)(startedGame)

      equipWeaponAssertions(result, startedGame)
    }

    "Given Red Cape" should {
      val result = equipActionHandler.handle(RedCape, uuid)(startedGame)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
      }

      "State the armour was equipped" in {
        assert(result.messages.map(_.text).exists(_.contains(s"${GameStateGenerator.playerName} puts on")))
      }

      "Equips the armour" in {
        assert(result.player.equipment.armour.nonEmpty)
      }

      "Affects the players equipment" in {
        assert(startedGame.player.equipment.weapon.isEmpty && startedGame.player.equipment.armour.isEmpty)
        assert(startedGame.player.equipment != result.player.equipment)
      }
    }

    "Given LeatherArmour" should {
      val result = equipActionHandler.handle(LeatherArmour, uuid)(startedGame)

      equipArmourAssertions(result, startedGame)
    }

    "Given Ringmail" should {
      val result = equipActionHandler.handle(Ringmail, uuid)(startedGame)

      equipArmourAssertions(result, startedGame)
    }


    "Given OrcishArmour" should {
      val result = equipActionHandler.handle(OrcishArmour, uuid)(startedGame)

      equipArmourAssertions(result, startedGame)
    }

    "Given Renarts Deceiver" should {
      val result = equipActionHandler.handle(RenartsDeceiver, uuid)(startedGame)

      equipWeaponAssertions(result, startedGame)
    }

    "Given Manamune" should {
      val result = equipActionHandler.handle(Manamune, uuid)(startedGame)

      equipWeaponAssertions(result, startedGame)
    }

    "Given Defiant Wand" should {
      val result = equipActionHandler.handle(WandOfDefiance, uuid)(startedGame)

      equipWeaponAssertions(result, startedGame)
    }


    def equipWeaponAssertions(result: GameState, startedGame: GameState): Unit = {
      equipStandardAssertions(result, startedGame)

      "State the weapon was equipped" in {
        assert(result.messages.map(_.text).exists(_.contains(s"${GameStateGenerator.playerName} equips")))
      }

      "Equips the weapon" in {
        assert(result.player.equipment.weapon.nonEmpty)
      }
    }

    def equipArmourAssertions(result: GameState, startedGame: GameState): Unit = {
      equipStandardAssertions(result, startedGame)

      "State the armour was equipped" in {
        assert(result.messages.map(_.text).exists(_.contains(s"${GameStateGenerator.playerName} puts on")))
      }

      "Equips the armour" in {
        assert(result.player.equipment.armour.nonEmpty)
      }
    }

    def equipStandardAssertions(result: GameState, startedGame: GameState): Unit = {
      lazy val numberOfNewMessages = result.messages.size - startedGame.messages.size
      lazy val latestMessages = result.messages.takeRight(numberOfNewMessages)

      "State the action was used" in {
        assert(result.messages.size > startedGame.messages.size)
      }

      "Reduce the player's position" in {
        assert(result.player.position < 100)
      }

      "the enemy does not take a turn" in {
        assert(result.player.stats.currentHp == startedGame.player.stats.currentHp)

        assert(latestMessages.exists(!_.text.contains("Slime ")))
      }

      "Affects the players equipment" in {
        assert(startedGame.player.equipment.weapon.isEmpty && startedGame.player.equipment.armour.isEmpty)
        assert(startedGame.player.equipment != result.player.equipment)
      }
    }
  }
}
