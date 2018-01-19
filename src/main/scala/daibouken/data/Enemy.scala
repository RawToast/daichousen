package daibouken.data

case class Enemy(meta: EnemyMetaData, enemyStats: EnemyStats, positionOffset: Int)

case class EnemyMetaData(name:String, className: String)
case class EnemyStats(stats: BasicStats, equipment: Equipment, status: Seq[Status])
