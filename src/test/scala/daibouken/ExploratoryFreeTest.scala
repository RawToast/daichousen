package daibouken

import java.util.UUID

import cats.data.State
import cats.arrow.FunctionK
import cats.~>
import daibouken.data._
import daibouken.free.{BattleCommandA, BattleEffect}
import daibouken.free.BattleEffect.{BattleCommand, DealDamage, EndTurn}
import org.scalatest.{Matchers, WordSpec}

class ExploratoryFreeTest extends WordSpec with Matchers {

  type BattleStateT[A] = State[BattleState, A]
  val pureCompiler: FunctionK[BattleCommandA, BattleStateT] = new (BattleCommandA ~> BattleStateT) {
    def apply[A](fa: BattleCommandA[A]): BattleStateT[A] =
      fa match {
        case DealDamage(_, amount) => State.modify((x: BattleState) =>
          // Calling out for a lens
          x.copy(player = x.player.copy(playerStats =
            x.player.playerStats.copy(stats =
              x.player.playerStats.stats.copy(currentHp = x.player.playerStats.stats.currentHp - amount)))))
          .asInstanceOf[BattleStateT[A]]
        case EndTurn() => State.inspect(_.asInstanceOf[A])
      }
  }

  "PureCompiler" when {

    val targetUUID = UUID.fromString("6fd1ced4-f47f-42ec-9363-dff1099e4a1f")
    val player = Player(PlayerMetaData("test", "test", targetUUID),
      PlayerStats(BasicStats(100, 100, 0, 0, 0, 0, 0), Experience(0, 0, 0, 0), Equipment(None, None), Seq.empty),
      NonCombatStats(0, 0), 100)
    val enemies = Seq.empty
    val messages = Seq.empty
    def initialState = BattleState(player, enemies, messages)

    "The player attacks an enemy" should {

      def attackProgram(target: UUID, damage: Int): BattleCommand[BattleState] =
        for {
          _ <- BattleEffect.dealDamage(target, damage)
          n <- BattleEffect.endTurn[BattleState]
        } yield n


      "Create a damage message" in {
        val resultCommand: BattleCommand[BattleState] = attackProgram(targetUUID, 5)

        val resultState: (BattleState, BattleState) = resultCommand.foldMap(pureCompiler).run(initialState).value

        val result = resultState._2

        result shouldNot be(initialState)
        result.player.playerStats.stats.currentHp should be < initialState.player.playerStats.stats.currentHp
      }

      "Be composable in many ways" in {
        import cats._
        val resultCommand: BattleCommand[BattleState] = attackProgram(targetUUID, 4)
        val resultCommand2: BattleCommand[BattleState] = attackProgram(targetUUID, 6)
        val evalState: Eval[BattleState] = resultCommand.foldMap(pureCompiler).runA(initialState)
          .flatMap(s => resultCommand2.foldMap(pureCompiler).runA(s))

        val altEval = for {
          step1 <- resultCommand.foldMap(pureCompiler).runA(initialState)
          step2 <- resultCommand2.foldMap(pureCompiler).runA(step1)
        } yield step2


        val seqEval: Eval[BattleState] = Seq(resultCommand, resultCommand2)
            .map(_.foldMap(pureCompiler))
            .foldLeft[Eval[BattleState]](Now(initialState))((currentState, commands) =>
                currentState.flatMap(x => commands.runA(x)))


        val seqResult = seqEval.value
        val altResult = altEval.value
        val result = evalState.value

        result shouldNot be(initialState)
        altResult shouldNot be(initialState)
        seqResult shouldNot be(initialState)

        result should be(altResult)
        result should be(seqResult)

        result.player.playerStats.stats.currentHp should be < initialState.player.playerStats.stats.currentHp
      }
    }
  }

}
