package chousen.game.core

import chousen.Optics._
import chousen.api.data._
import chousen.game.actions.DamageCalculator
import chousen.game.core.turn.{PlayerDamageUtil, PostTurnOps}
import chousen.game.status.StatusCalculator

import scala.annotation.tailrec
import scala.util.{Left, Random, Right}


object GameOps extends GameOps(new EncounterOp(new StatusCalculator), new DamageCalculator(new StatusCalculator))

abstract class GameOps(encOps: EncounterOps, damageCalculator: DamageCalculator) {

  final def updateUntilPlayerIsActive(ed: EncounterData): EncounterData =
    updateUntilPlayerIsActive(ed._1, ed._2, ed._3)


  @scala.annotation.tailrec
  final def updateUntilPlayerIsActive(player: Player, enemies: Set[Enemy], messages: Seq[GameMessage]): EncounterData = {
    val next: (Player, Set[Enemy], Seq[GameMessage]) = update(player, enemies, messages)
    encOps.getActive(next) match {
      case Left(_) => next
      case Right(_) =>
        val (p, es, msgs) = EnemyTurnOps.takeTurn(next._1, next._2, next._3)(damageCalculator)

        updateUntilPlayerIsActive(p, es, msgs)
    }
  }

  // Note Left side is inactive, Right is active
  def isGameActive(ed: EncounterData): Boolean = {
    val (player, enemies, _) = ed
    if (0 >= player.stats.currentHp || enemies.isEmpty) false
    else true
  }


  def update(player: Player, enemies: Set[Enemy], messages: Seq[GameMessage]): EncounterData = {

    def process: EncounterUpdate = encOps.ensureActive _ andThen encOps.announceActive

    process(Tuple3(player, enemies, messages))
  }
}


object EnemyTurnOps {

  // This method will use the enemy with the highest position
  def takeTurn(player: Player, enemies: Set[Enemy], messages: Seq[GameMessage])(implicit dc: DamageCalculator): EncounterData = {

    val activeEnemy = enemies.maxBy(_.position)

    //TODO: Move elsewhere

    def diceRoll = Random.nextInt(6) + 1

    // Crap AI code was here
    val afterDmgGame = PlayerDamageUtil.doDamage(player, enemies, messages, activeEnemy)(dc)

    val statusHandler: ((Player, Set[Enemy], Seq[GameMessage])) => (Player, Set[Enemy], Seq[GameMessage]) =
      handlePerTurnStatuses(activeEnemy)
    statusHandler.andThen(PostTurnOps.handleDead)(afterDmgGame)
  }


  def handlePerTurnStatuses(ae: Enemy)(pem: (Player, Set[Enemy], Seq[GameMessage])): EncounterData = {
    val (p, es, ms) = pem
    var msgs = Seq.empty[GameMessage]

    import cats.implicits.catsSyntaxSemigroup
    import cats.instances.int.catsKernelStdGroupForInt
    import cats.instances.option.catsKernelStdMonoidForOption

    def regenEffects(e: Enemy) = e.status.filter(s => s.effect == Regen)
      .reduceLeftOption[Status] { case (a, b) => a.copy(amount = a.amount |+| b.amount) }

    def burnEffects(e: Enemy) = e.status.filter(s => s.effect == Burn)
      .reduceLeftOption[Status] { case (a, b) => a.copy(amount = a.amount |+| b.amount) }

    def effectsForComputation(e: Enemy): Seq[Status] =
      e.status.filterNot(ef => ef.effect == Regen || ef.effect == Burn) ++ regenEffects(e) ++ burnEffects(e)

    def foldStatus(e: Enemy, s: Status) = {
      s.effect match {
        case Poison => e
        case Burn => {
          val dmg = s.amount.getOrElse(0) + p.experience.level
          def doDmg(i:Int) = Math.max(0, i - dmg)
          msgs = msgs :+ GameMessage(s"${e.name} burns for $dmg damage")

          EnemyHpLens.modify(doDmg)(e)
        }
        case Fear => {
          if (e.stats.currentHp < s.amount.getOrElse(1) ||
            (e.stats.currentHp.toDouble / e.stats.maxHp.toDouble) <= (s.amount.getOrElse(1).toDouble / 100d)) {
            EnemyHpLens.set(-666)(e)
          } else {
            e
          }
        }
        case _ => e
      }
    }
    def handleStatus(e: Enemy) =
      effectsForComputation(e).foldLeft(e)(foldStatus)


    def reducePerTurnStatus(e: Enemy) = e.copy(status = e.status.map(sf => sf.effect match {
      case Burn => sf.copy(turns = sf.turns - 1)
      case _ => sf
    }))

    def removeDeadStatuses(e: Enemy) = e.copy(status = e.status.filter(_.turns > 0))


    val updateEnemy: (Enemy) => Enemy = e => if(e.id == ae.id) {
      (handleStatus _).andThen(reducePerTurnStatus).andThen(removeDeadStatuses)(e) } else e

    (p, es.map(updateEnemy), ms ++ msgs)
   }
}


final class EncounterOp(sc: StatusCalculator) extends EncounterOps {

  @tailrec
  override def ensureActive(encounterData: EncounterData): EncounterData = {
    import chousen.Implicits._

    // At THIS point, status effects duration should be reduced -- since it would be PER tick.

    val (p, es, msgs) = encounterData
    val sePlayer = sc.calculate(p)

    val (player, enemies) = p.copy(position = p.position + sePlayer.stats.speed) ->
      es.map(e => e.copy(position = e.position + sc.calculate(e).stats.speed))

    val maxPosition = math.max(player.position, if (enemies.isEmpty) 0 else enemies.maxBy(_.position).position)
    lazy val numWithMaxPosition = if (player.position == maxPosition) 1 + enemies.count(_.position == maxPosition)
    else enemies.count(_.position == maxPosition)


    if (maxPosition < 100) ensureActive((player, enemies, msgs))
    else {
      lazy val withPosition = (es: Set[Enemy]) => es.filter(_.position >= maxPosition)
      lazy val enemiesWithPosition = withPosition(enemies)
      lazy val fastestEnemySpeed = enemiesWithPosition.map(e => sc.calculate(e)).maxBy(_.stats.speed).stats.speed

      numWithMaxPosition match {
        case 1 => (player, enemies, msgs)
        case 2 if player.position == maxPosition =>
          if (sePlayer.stats.speed != fastestEnemySpeed) ensureActive((player, enemies, msgs))
          else {
            val incEnemies: Set[Enemy] = enemies.map(e =>
              if (e ~= enemies.maxBy(_.position)) {
                e.copy(position = e.position + 1)
              } else e)

            ensureActive(Tuple3(player, incEnemies, msgs))
          }
        case _ if player.position == maxPosition =>
          if (sePlayer.stats.speed != fastestEnemySpeed) ensureActive((player, enemies, msgs))
          else {
            val fastestSpeeds = enemiesWithPosition.filter(_.stats.speed == fastestEnemySpeed)

            // Remove enemies without max speed (recurse)
            if (enemiesWithPosition.size > fastestSpeeds.size) {
              ensureActive(Tuple3(player, enemies, msgs))
            } else {
              // Player speed equals fastest enemy (a min of 2 have same speed)
              // All same speed
              val chosenOne: Enemy = enemiesWithPosition.maxBy(_.id)
              val nextEnemies: Set[Enemy] =
                enemies.map(e => if (e ~= chosenOne) e.copy(position = e.position + 1) else e)

              ensureActive(Tuple3(player, nextEnemies, msgs))
            }
          }
        case _ =>
          //enemiesWithPosition.map(e => e.copy(position = e.stats.speed + e.position))
          val fastestSpeeds = enemiesWithPosition.map(sc.calculate).filter(_.stats.speed == fastestEnemySpeed)

          fastestSpeeds.size match {
            case 1 => ensureActive((player, enemies, msgs))
            case _ =>
              if (fastestSpeeds.size < enemiesWithPosition.size) ensureActive((player, enemies, msgs))
              else {
                val chosenOne: Enemy = enemiesWithPosition.maxBy(_.id)
                val nextEnemies = enemies.map(e => if (e ~= chosenOne) e.copy(position = e.position + 1)
                else e)
                ensureActive((player, nextEnemies, msgs))
              }
          }
      }
    }
  }

  override def announceActive(encounterData: EncounterData): EncounterData = {
    val (player, enemies, msgs) = encounterData

    val fastestEnemy = if (enemies.isEmpty) 0 else enemies.maxBy(_.position).position

    val newMessages = if (player.position > fastestEnemy) msgs :+ GameMessage(s"${player.name}'s turn.")
    else msgs

    (player, enemies, newMessages)
  }

  override def getActive(encounterData: EncounterData): Either[Player, Enemy] = {
    val (p: Player, es: Set[Enemy], _) = encounterData
    val maxPosition = math.max(p.position, if (es.isEmpty) 0 else es.maxBy(_.position).position)

    if (p.position == maxPosition) Left(p)
    else Right(es.maxBy(_.position))
  }
}

trait EncounterOps {
  def ensureActive(encounterData: EncounterData): EncounterData

  def announceActive(encounterData: EncounterData): EncounterData

  def getActive(encounterData: EncounterData): Either[Player, Enemy]
}

