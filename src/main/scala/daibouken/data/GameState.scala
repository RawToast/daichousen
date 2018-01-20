package daibouken.data

case class Battle(player: Player, enemies: Seq[Enemy], messages: Seq[GameMessage])

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


case class Requirements(str: Int, dex: Int, int: Int)