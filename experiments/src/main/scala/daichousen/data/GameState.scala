package daichousen.data

import monocle.Lens
import monocle.macros.GenLens

case class Battle(player: Player, enemies: Seq[Enemy], messages: Seq[GameMessage])

object Battle {
  val playerCurrentHealth: Lens[Battle, Int] = GenLens[Battle](_.player.playerStats.stats.currentHp)
  val playerPosition: Lens[Battle, Int] = GenLens[Battle](_.player.positionOffset)

  val gameMessages: Lens[Battle, Seq[GameMessage]] = GenLens[Battle](_.messages)
}

case class GameMessage(text: String)


case class BasicStats(maxHp: Int,
                      currentHp: Int,
                      strength: Int,
                      dexterity: Int,
                      intellect: Int,
                      vitality: Int,
                      speed: Int)


case class Status(effect: StatusEffect, description: String, turns: Int, amount: Option[Int] = None)

sealed trait StatusEffect
case class Slow() extends StatusEffect


case class Requirements(str: Int, dex: Int, int: Int)