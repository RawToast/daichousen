package daibouken.free

import java.util.UUID

import cats.free.Free
import cats.free.Free.liftF

sealed trait BattleEffect[T]

case class DealDamage(enemyId: UUID, amount: Int) extends BattleEffect[Unit]
case class EndTurn[T]() extends BattleEffect[T]

object BattleEffects {
  type FreeBattleEffect[A] = Free[BattleEffect, A]

  def dealDamage(target: UUID, damage: Int): Free[BattleEffect, Unit] =
    liftF[BattleEffect, Unit](DealDamage(target, damage))
  def endTurn[T]: FreeBattleEffect[T]=
    liftF[BattleEffect, T](EndTurn[T]())
}
