package chousen.game.actions

import java.util.UUID

import chousen.Optics._
import chousen.api.data._
import chousen.game.core.turn.PositionCalculator
import chousen.util.LensUtil

class EquipmentActionHandler {

  def handle(action: EquipAction, uuid: UUID): (GameState) => GameState = {
    LensUtil.duoLens(PlayerLens, MessagesLens).modify{
      case (p:Player, msgs: Seq[GameMessage]) =>
        actions(action)(p, msgs, uuid)
    }
  }

  private def actions(action: EquipAction): (Player, Seq[GameMessage], UUID) => (Player, Seq[GameMessage]) = {
    action match {
      case Club => club
      case TrollCrusher => trollCrusher

      case LeatherArmour => leatherArmour
      case Ringmail => ringmail
      case OrcishArmour => orcArmour

      // Treasure
      case RedCape => redCape
      case RenartsDeceiver => rensDeceiver
      case Manamune => manamune
      case WandOfDefiance => wandOfDef
    }
  }

  def weapon(name: String, dmg: Int, effects: Seq[WeaponEffect]= Seq.empty) = (p: Player, msgs: Seq[GameMessage], uuid: UUID) => {

    val message = GameMessage(s"${p.name} equips $name.")

    val lens = PlayerWeaponLens.set(Option(Weapon(uuid, name, dmg, effects = effects)))
      .andThen(PositionCalculator.calculatePosition(_: Player, cost = PositionCalculator.ENHANCED))

    lens.apply(p) -> (msgs :+ message)
  }

  def club(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) =
    weapon("Club", 3)(p, msgs, uuid)

  def trollCrusher(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) =
    weapon("Troll Crusher", 9, Seq(Crush))(p, msgs, uuid)


  // Armour
  def leatherArmour(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) = {
    armour("Leather Armour", 4)(p, msgs, uuid)
  }

  def ringmail(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) = {
    armour("Ringmail", 6)(p, msgs, uuid)
  }

  //Treasure Only
  def redCape(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) = {
    armour("Red Cape", 5, pen = PositionCalculator.FAST)(p, msgs, uuid)
  }

  def orcArmour(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) = {
    armour("Orcish Armour", 21)(p, msgs, uuid)
  }


  def rensDeceiver(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) =
    weapon("Renart's Deciever", 10, Seq(Quick, Deceive))(p, msgs, uuid)

  def manamune(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) =
    weapon("Manamune", 3, Seq(Magic, Magic))(p, msgs, uuid)

  def wandOfDef(p: Player, msgs: Seq[GameMessage], uuid: UUID): (Player, Seq[GameMessage]) =
    weapon("Defiant Wand", 4, Seq(Protection))(p, msgs, uuid)

  private def armour(name: String, ac: Int, pen:Int = PositionCalculator.TURTLE) = (p: Player, msgs: Seq[GameMessage], uuid: UUID) => {
    val message = GameMessage(s"${p.name} puts on $name.")

    val lens = PlayerArmourLens.set(Option(Armour(uuid, name, ac)))
      .andThen(PositionCalculator.calculatePosition(_: Player, cost = pen))

    lens.apply(p) -> (msgs :+ message)
  }

}
