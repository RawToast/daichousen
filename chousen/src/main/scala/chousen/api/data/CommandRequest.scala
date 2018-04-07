package chousen.api.data

import java.util.UUID

import io.circe.{Encoder, Decoder}
import io.circe.generic.extras.semiauto.{deriveEnumerationEncoder, deriveEnumerationDecoder}

sealed trait CommandRequest

final case class AttackRequest(targetId: UUID) extends CommandRequest

final case class BlockRequest() extends CommandRequest

final case class SelfInflictingActionRequest(action: SelfAction) extends CommandRequest

final case class SingleTargetActionRequest(targetId: UUID, action: SingleTargetAction) extends CommandRequest

final case class MultiTargetActionRequest(targetIds: Set[UUID], action: MultiAction) extends CommandRequest

final case class CardActionRequest(action: CardAction, cardId: Option[UUID]) extends CommandRequest

final case class CampfireActionRequest(action: CampFireAction, cardId: Option[UUID]) extends CommandRequest


final case class EquipmentActionRequest(id: UUID, action: EquipAction) extends CommandRequest

sealed trait Action
object Action {
  implicit def actionEncoder: Encoder[Action] = deriveEnumerationEncoder[Action]
  implicit def actionDecoder: Decoder[Action] = deriveEnumerationDecoder[Action]
}

sealed trait SingleTargetAction extends Action
sealed trait MultiAction extends Action
sealed trait SelfAction extends Action
sealed trait CardAction extends Action
sealed trait CampFireAction extends Action


sealed trait StandardCardAction extends CardAction
sealed trait DiscardCardAction extends CardAction

sealed trait EquipAction extends Action

sealed trait EquipWeapon extends EquipAction
sealed trait EquipArmour extends EquipAction
sealed trait EquipJewelery extends EquipAction


case object CrushingBlow extends SingleTargetAction

case object Fireball extends MultiAction

case object HealWounds extends SelfAction
case object Haste extends SelfAction
case object PotionOfRage extends SelfAction

case object EssenceOfStrength extends SelfAction
case object EssenceOfDexterity extends SelfAction
case object EssenceOfIntelligence extends SelfAction
case object EssenceOfVitality extends SelfAction


case object Rummage extends StandardCardAction
case object BagOfGold extends StandardCardAction


sealed trait DiscardingCampFireAction extends CampFireAction
case object Rest extends CampFireAction
case object Explore extends CampFireAction
case object RestAndExplore extends CampFireAction
case object Drop extends DiscardingCampFireAction
case object Destroy extends DiscardingCampFireAction
case object LearnSkill extends DiscardingCampFireAction



case object Club extends EquipWeapon
case object TrollCrusher extends EquipWeapon
case object RenartsDeceiver extends EquipWeapon
case object Manamune extends EquipWeapon
case object WandOfDefiance extends EquipWeapon



case object LeatherArmour extends EquipArmour
case object Ringmail extends EquipArmour
case object RedCape extends EquipArmour
case object OrcishArmour extends EquipArmour

