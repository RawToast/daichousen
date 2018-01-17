package daibouken.data

import java.util.UUID


case class GameState(uuid: UUID, player: Player)


case class GameMessage(text: String)


case class BasicStats(maxHp: Int,
                      currentHp: Int,
                      strength: Int,
                      dexterity: Int,
                      intellect: Int,
                      vitality: Int,
                      speed: Int)


case class Player(meta: PlayerMetaData, playerStats: PlayerStats, gold: Int, positionOffset: Int)

case class PlayerMetaData(name:String, className: String)
case class PlayerStats(stats: BasicStats, experience: Experience, equipment: Equipment, status: Seq[Status])

case class Experience(level:Int, current: Long, next: Long, total: Long)



case class Enemy(meta: EnemyMetaData, enemyStats: EnemyStats, positionOffset: Int)

case class EnemyMetaData(name:String, className: String)
case class EnemyStats(stats: BasicStats, equipment: Equipment, status: Seq[Status])


case class Status(effect: StatusEffect, description: String, turns: Int, amount: Option[Int] = None)
sealed trait StatusEffect


case class Equipment(weapon: Option[Weapon], armour: Option[Armour])

case class Weapon(name: String, dmg: Int, requirements: Requirements, effects: Seq[WeaponEffect])
sealed trait WeaponEffect

case class Armour(name: String, defense: Int, requirements: Requirements)

case class Requirements(str: Int, dex: Int, int: Int)