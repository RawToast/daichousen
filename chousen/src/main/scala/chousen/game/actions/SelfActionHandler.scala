package chousen.game.actions

import chousen.Optics._
import chousen.api.data._
import chousen.game.core.turn.PositionCalculator.{FAST, calculatePosition}
import chousen.game.status.{StatusBuilder, StatusCalculator}
import chousen.util.LensUtil
import monocle.Lens


class SelfActionHandler(sc: StatusCalculator) extends ActionHandler {

  def handle(action: SelfAction): (GameState) => GameState = {
    LensUtil.triLens(PlayerLens, CardsLens, MessagesLens).modify {
      case (p: Player, cs: Cards, msgs: Seq[GameMessage]) =>
        actions(action)(p, cs, msgs)
    }.andThen(handleDead)
  }

  private def actions(actionId: SelfAction): (Player, Cards, Seq[GameMessage]) => (Player, Cards, Seq[GameMessage]) =
    actionId match {
      case HealWounds => healWounds
      case PotionOfRage => rage
      case Haste => haste

      case EssenceOfStrength => essenceOfStrength
      case EssenceOfDexterity => essenceOfDexterity
      case EssenceOfVitality => essenceOfVitality
      case EssenceOfIntelligence => essenceOfIntellect
    }

  type Update = (Player, Cards, Seq[GameMessage])


  def healWounds(p: Player, cs: Cards, msgs: Seq[GameMessage]): Update = {
    val sePlayer = sc.calculate(p)

    val healAmount = 15 + (2 + sePlayer.stats.intellect) + (p.stats.maxHp / 10) + sePlayer.experience.level
    val message = GameMessage(s"${p.name} uses Heal Wounds and recovers ${healAmount}HP!")
    val gameMessages = msgs :+ message

    val lens = LensUtil.duoLens(PlayerHealthLens, PlayerPositionLens)
               .modify { case (hp: Int, position: Int) =>
                 Math.min(p.stats.maxHp, hp + healAmount) -> (position - 100)
               }
    (lens.apply(p), cs, gameMessages)
  }


  def essenceOfStrength(p: Player, cs: Cards, msgs: Seq[GameMessage]): Update = {
    essence(p, cs, msgs, "Strength", PlayerStrengthLens)
  }

  def essenceOfDexterity(p: Player, cs: Cards, msgs: Seq[GameMessage]): Update = {
    essence(p, cs, msgs, "Dexterity", PlayerDexterityLens)
  }

  def essenceOfIntellect(p: Player, cs: Cards, msgs: Seq[GameMessage]): Update = {
    essence(p, cs, msgs, "Intellect", PlayerIntellectLens)
  }

  def essenceOfVitality(p: Player, cs: Cards, msgs: Seq[GameMessage]): Update = {
    essence(p, cs, msgs, "Vitality", PlayerVitalityLens)
  }

  private def essence(p: Player, cs: Cards, msgs: Seq[GameMessage], stat: String, lens: Lens[Player, Int]) = {
    if (cs.playedEssence) (p, cs, msgs)
    else {
      val bonusStat = 1
      val message = GameMessage(s"${p.name} uses Essence of $stat and gains $bonusStat $stat!")
      val gameMessages = msgs :+ message

      val effect = lens.modify { x =>
        Math.min(p.stats.maxHp, x + bonusStat)
      }
      (effect.apply(p), cs.copy(playedEssence = true), gameMessages)
    }
  }

  def haste(p: Player, cs: Cards, msgs: Seq[GameMessage]) = {
    val message = GameMessage(s"${p.name} uses Haste!")

    val hasteStatus: Status = StatusBuilder.makeHaste(4)

    (PlayerStatusLens.modify(_ :+ hasteStatus)
     //      .andThen(PlayerSpeedLens.modify(_ + 4))
     .andThen(calculatePosition(_: Player, cost = FAST))(p), cs, msgs :+ message)
  }

  def rage(p: Player, cs: Cards, msgs: Seq[GameMessage]) = {
    val message = GameMessage(s"${p.name} drinks a Potion of Rage!")

    val status: Status = StatusBuilder.makeBerserk(4, turns = 8)

    (PlayerStatusLens.modify(_ :+ status)
     .andThen(potionPositionCalc)(p), cs, msgs :+ message)
  }

  private val potionPositionCalc = calculatePosition(_: Player, cost = FAST)
}
