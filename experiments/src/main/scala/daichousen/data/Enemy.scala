package daichousen.data

import java.util.UUID

case class Enemy(id: UUID, meta: EnemyMetaData, enemyStats: EnemyStats, positionOffset: Int)

case class EnemyMetaData(name: String)

case class EnemyStats(stats: BasicStats, equipment: Equipment, status: Seq[Status])
