package chousen.game.dungeon

import chousen.api.data.{Battle, Dungeon, Enemy}

trait DungeonBuilder {
  def makeDungeon(dungeonSeed:Int,
                  dungeonSeed2:Int,
                  dungeonSeed3:Int): Dungeon
}

case class BattleBuilder(battle: Battle=Battle(Set.empty[Enemy])){

  def +(e: Enemy): BattleBuilder = {
    copy(this.battle.copy(this.battle.enemies + e))
  }
}

class SimpleDungeonBuilder() extends DungeonBuilder with EnemyBuilder {


  def makeDungeon(dungeonSeed:Int = new scala.util.Random().nextInt(6),
                  dungeonSeed2:Int = new scala.util.Random().nextInt(6),
                  dungeonSeed3:Int = new scala.util.Random().nextInt(6)): Dungeon = {

    val battle2: Battle = {dungeonSeed match {
      case (0 | 1 | 2) => BattleBuilder() + createRat + createRat + createRat + createRat
      case (3 | 4) => BattleBuilder() + gnoll + createSlime
      case _ => BattleBuilder() + giantWorm + createRat
    }}.battle

    val orcBattle = {dungeonSeed3 match {
      case (0 | 1 ) => BattleBuilder() + orcWarriorD + orcWizard + orcWarriorQ
      case (2 | 3) => BattleBuilder() + orcWarriorS + orcWizard + orcWarriorQ
      case _ => BattleBuilder() + orcWarriorS + orcWizard + orcWarriorD
    }}.battle


     val b11 = (BattleBuilder() + kobold + knollShaman + kobold + totem).battle
     val b12 = (BattleBuilder() + orcWizard + hugeGolem + orcWizard).battle


    val b13 = (BattleBuilder() + gKnollShaman + draconian + orcPriest).battle
    val b14 = (BattleBuilder() + smallOrc + tripleOrc + smallOrc).battle

    def campfire = Battle(Set(campFire))


    Dungeon(battle2, Seq(campfire,
      orcBattle, campfire,
      b11, b12, campfire,
      b13, b14, campfire,
      Battle(Set(kraken, totem)), campfire,
      Battle(Set(fireOrcKing, totem)), campfire,
      Battle(Set(orcKing))
    ))
  }
}
