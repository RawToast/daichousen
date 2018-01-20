package daibouken.free

import java.util.UUID

import cats.free.Free
import cats.free.Free.liftF

sealed trait BattleCommandA[T]

object BattleEffect {
  type BattleCommand[A] = Free[BattleCommandA, A]

  case class DealDamage(enemyId: UUID, amount: Int) extends BattleCommandA[Unit]

  case class EndTurn[T]() extends BattleCommandA[T]

  def dealDamage(target: UUID, damage: Int): Free[BattleCommandA, Unit] =
    liftF[BattleCommandA, Unit](DealDamage(target, damage))

  def endTurn[T]: Free[BattleCommandA, T] =
    liftF[BattleCommandA, T](EndTurn[T]())
}
