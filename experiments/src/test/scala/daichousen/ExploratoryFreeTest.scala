package daichousen

import java.util.UUID

import cats.data.State
import cats.arrow.FunctionK
import cats.{Eval, Now, ~>}
import daichousen.data.{GameMessage, _}
import daichousen.free._
import daichousen.free.BattleEffects.FreeBattleEffect
import daichousen.data._
import daichousen.free.{BattleEffect, BattleEffects}
import org.scalatest.{Matchers, WordSpec}

class ExploratoryFreeTest extends WordSpec with Matchers {

  "PureCompiler" when {

    val targetUUID = UUID.fromString("6fd1ced4-f47f-42ec-9363-dff1099e4a1f")
    val player = Player(PlayerMetaData("test", "test", targetUUID),
      PlayerStats(BasicStats(100, 100, 5, 5, 5, 5, 5), Experience(0, 0, 0, 0), Equipment(None, None), Seq.empty),
      NonCombatStats(0, 0), 100)
    val enemies = Seq.empty
    val messages = Seq.empty
    def initialState = Battle(player, enemies, messages)

    "The player is the target of an attack" should {

      def attackProgram(target: UUID): FreeBattleEffect[Battle] =
        for {
          dmg <- BattleEffects.calculatePlayerDamage(targetUUID)
          _ <- BattleEffects.dealDamage(target, dmg)
          n <- BattleEffects.endTurn[Battle]
        } yield n


      "Reduce the player's health damage message" in {
        val resultCommand: FreeBattleEffect[Battle] = attackProgram(targetUUID)

        val result = resultCommand.foldMap(pureCompiler).runA(initialState).value

        result shouldNot be(initialState)
        result.player.playerStats.stats.currentHp should be < initialState.player.playerStats.stats.currentHp
      }

      "Be a composable action that can be constructed in many ways" in {

        // Two commands
        val resultCommand: FreeBattleEffect[Battle] = attackProgram(targetUUID)
        val resultCommand2: FreeBattleEffect[Battle] = attackProgram(targetUUID)

        // Basic composition
        val evalState: Eval[Battle] = resultCommand.foldMap(pureCompiler).runA(initialState)
          .flatMap(s => resultCommand2.foldMap(pureCompiler).runA(s))

        // For comp syntactic sugar
        val altEval = for {
          step1 <- resultCommand.foldMap(pureCompiler).runA(initialState)
          step2 <- resultCommand2.foldMap(pureCompiler).runA(step1)
        } yield step2

        // Evaluation from a sequence of programs (imagine prefixing or appending common steps)
        val seqEval: Eval[Battle] = Seq(resultCommand, resultCommand2)
            .map(_.foldMap(pureCompiler))
            .foldLeft[Eval[Battle]](Now(initialState))((currentState, commands) =>
                currentState.flatMap(x => commands.runA(x)))


        val seqResult = seqEval.value
        val altResult = altEval.value
        val result = evalState.value

        // Change game state
        result shouldNot be(initialState)
        altResult shouldNot be(initialState)
        seqResult shouldNot be(initialState)

        // All solutions should return the same result
        result should be(altResult)
        result should be(seqResult)
      }
    }
  }

  type BattleState[A] = State[Battle, A]

  val pureCompiler: FunctionK[BattleEffect, BattleState] = new (BattleEffect ~> BattleState) {
    def apply[A](fa: BattleEffect[A]): BattleState[A] = {
      // Note `asInstanceOf[A]` does nothing, it simply hides IntelliJ false warnings.
      fa match {
        case DealDamage(_, amount) => State.modify(Battle.playerCurrentHealth.modify(_ - amount)).asInstanceOf[BattleState[A]]
        case AppendMessage(msg: String) => State.modify(Battle.gameMessages.modify(_ :+ GameMessage(msg))).asInstanceOf[BattleState[A]]
        case CalculatePlayerDamage(_) => State.inspect(_.player.playerStats.stats.strength.asInstanceOf[A])
        case CalculatePlayerMagicDamage(_) => State.inspect(_.player.playerStats.stats.intellect.asInstanceOf[A])
        case ApplyStatus(_, _) => State.modify((b: Battle) => b).asInstanceOf[BattleState[A]]
        case ChangeOffset(_, amount) => State.modify(Battle.playerPosition.modify(_ + amount)).asInstanceOf[BattleState[A]]
        case EndTurn() => State.inspect(_.asInstanceOf[A])
      }
    }
  }
}


