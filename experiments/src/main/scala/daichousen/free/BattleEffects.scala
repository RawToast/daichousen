package daichousen.free

import java.util.UUID

import cats.free.Free
import cats.free.Free.liftF
import daichousen.data.Status

sealed trait BattleEffect[T]

case class DealDamage(target: UUID, amount: Int) extends BattleEffect[Unit]
case class ChangeOffset(target: UUID, amount: Int) extends BattleEffect[Unit]
case class ApplyStatus(target: UUID, status: Status) extends BattleEffect[Unit]
case class AppendMessage(message: String) extends BattleEffect[Unit]

case class CalculatePlayerDamage(target: UUID) extends BattleEffect[Int]
case class CalculatePlayerMagicDamage(target: UUID) extends BattleEffect[Int]

case class EndTurn[T]() extends BattleEffect[T]

object BattleEffects {
  type FreeBattleEffect[A] = Free[BattleEffect, A]

  def dealDamage(target: UUID, damage: Int): FreeBattleEffect[Unit] =
    liftF[BattleEffect, Unit](DealDamage(target, damage))

  def endTurn[T]: FreeBattleEffect[T]=
    liftF[BattleEffect, T](EndTurn[T]())

  def appendMessage(message: String): FreeBattleEffect[Unit] =
    liftF[BattleEffect, Unit](AppendMessage(message))

  def changeOffset(target: UUID, amount: Int): FreeBattleEffect[Unit] =
    liftF[BattleEffect, Unit](ChangeOffset(target, amount))

  def calculatePlayerDamage(target: UUID): FreeBattleEffect[Int] =
  liftF[BattleEffect, Int](CalculatePlayerDamage(target))

  def calculatePlayerMagicDamage(target: UUID): FreeBattleEffect[Int] =
    liftF[BattleEffect, Int](CalculatePlayerDamage(target))

  def applyStatus(target: UUID, status: Status): FreeBattleEffect[Unit] =
    liftF[BattleEffect, Unit](ApplyStatus(target, status))
}
