package daibouken.data

case class Player(meta: PlayerMetaData, playerStats: PlayerStats, otherStats: NonCombatStats, positionOffset: Int)

case class PlayerMetaData(name:String, className: String)
case class PlayerStats(stats: BasicStats, experience: Experience, equipment: Equipment, status: Seq[Status])
case class NonCombatStats(gold: Int, faith: Int)

case class Experience(level:Int, current: Long, next: Long, total: Long)