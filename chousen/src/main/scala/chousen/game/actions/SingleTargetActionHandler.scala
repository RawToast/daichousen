package chousen.game.actions

import java.util.UUID

import chousen.Optics._
import chousen.api.data._
import chousen.game.actions.Multipliers.{builder, highMulti, medMulti}
import chousen.game.core.turn.PositionCalculator._

class SingleTargetActionHandler(damageCalculator: DamageCalculator) extends ActionHandler {

  def handle(targetId: UUID, action: SingleTargetAction): (GameState) => GameState = {
    targettedLens(targetId).modify {
      case (p, es, msgs) =>
        es match {
          case Some(e) => singleTargetActions(action)(p, e, msgs)
          case None => (p, es, msgs)
        }
    }.andThen(handleDead)
  }

  private def singleTargetActions(actionId: SingleTargetAction): (Player, Enemy, Seq[GameMessage]) => (Player, Option[Enemy], Seq[GameMessage]) =
    actionId match {
      case CrushingBlow => crushingBlow
    }


  def ability(p: Player, e: Enemy, msgs: Seq[GameMessage])(
    useMsg: (String, String) => String = (p: String, e: String) => s"$p uses non-descript ability on $e.",
    damageMsg: (String, Int) => String = (e: String, d: Int) => s"$e takes $d damage.",
    multi: Multipliers = new Multipliers(),
    bonusDamage: Int = 0,
    damageCalc: (Player, Enemy, Multipliers) => Int = damageCalculator.calculatePlayerDamage,
    enemyEffect: Enemy => Enemy = e => e,
    speed: Int = STANDARD,
    goldCost: Int=0): (Player, Option[Enemy], Seq[GameMessage]) = {

    val sePlayer = damageCalculator.sc.calculate(p)

    val dmg = damageCalc(p, e, multi) + bonusDamage

    val targetMsg = GameMessage(useMsg(sePlayer.name, e.name))
    val dmgMsg = GameMessage(damageMsg(e.name, dmg))

    val newEnemy = EnemyStatsLens.composeLens(HpLens)
      .modify(hp => hp - dmg)
      .andThen(enemyEffect)(e)
    val gameMessages = msgs :+ targetMsg :+ dmgMsg

    val updatedPlayer = p.copy(position = calculatePosition(sePlayer, speed).position)
    val finalPlayer = updatedPlayer.copy(gold = updatedPlayer.gold - goldCost)
    (finalPlayer, Option(newEnemy), gameMessages)
  }

  type ActionUpdate = (Player, Option[Enemy], Seq[GameMessage])

  def crushingBlow(p: Player, e: Enemy, msgs: Seq[GameMessage]): ActionUpdate = ability(p, e, msgs)(
      useMsg = (p, e) => s"$p lands a crushing blow on $e!",
      multi = builder.strMulti(highMulti).dexMulti(medMulti).m,
      speed = SLUGGISH
    )
}
