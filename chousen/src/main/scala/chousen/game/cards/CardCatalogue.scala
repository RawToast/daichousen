package chousen.game.cards

import java.util.UUID

import chousen.api.data._

object CardCatalogue extends Potions with PermanentEffects with Utility with CampFire with Equipment
  with Strength with Dexterity with Magic
  with TreasureCards {

  val treasureDeck: Seq[Card] =
    Seq(
      rage,

      bagOfGold, bagOfGold, bagOfGold, bagOfGold,
      manamune, wandOfDefiance, deceiver,
      redCape, orcishArmour,

      club, ringmail,
    ).map(c => c.copy(treasure = true))


  // Deck built around stun/counter ST
  def fighterDeck: Seq[Card] = // 15
    Seq(
      healWounds, healWounds, healWounds, healWounds,
      haste, haste, haste, haste,

      essenceOfStrength, essenceOfStrength, essenceOfStrength, essenceOfStrength, // 16 essences
      essenceOfStrength, essenceOfStrength, essenceOfStrength, essenceOfStrength,
      essenceOfStrength, essenceOfStrength,
      essenceOfDexterity, essenceOfDexterity, essenceOfDexterity, essenceOfDexterity,
      essenceOfDexterity, essenceOfDexterity,

      club, trollCrusher, manamune, // 5 equips
      redCape, ringmail,

      crushingBlow, crushingBlow, crushingBlow, crushingBlow,

      rummage, rummage, rummage, rummage,
      bagOfGold, bagOfGold, bagOfGold, bagOfGold
    )

  def passiveCards: Seq[Card] = Seq(rest, explore, restAndExplore, drop, destroy, learnSkill)
}

sealed trait CardBuilder {
  def mkCard(name: String, description: String, action: Action, charges: Int = 0, requirements: Requirements = Requirements(),
             cost: Int = 0) =
    Card(UUID.randomUUID(), name, description, action,
      if (charges == 0) None else Some(charges), if (charges == 0) None else Some(charges),
      requirements, treasure = false, cost)

  def mkEquip(name: String, description: String, action: Action, requirements: Requirements = Requirements()) =
    Card(UUID.randomUUID(), name, description, action, None, None, requirements)
}

trait Potions extends CardBuilder {
  def haste: Card = Card(UUID.randomUUID(), "Potion of Haste", "Temporary increases player speed", Haste)

  def rage: Card = Card(UUID.randomUUID(), "Potion of Rage", "Temporary increases health, damage, and speed", PotionOfRage)
}

trait PermanentEffects extends CardBuilder {

  def essenceOfStrength: Card = mkCard("Essence of Strength", "Immediately increases Strength, only 1 essence may be played per turn", EssenceOfStrength)

  def essenceOfDexterity: Card = mkCard("Essence of Dexterity", "Immediately increases Dexterity, only 1 essence may be played per turn", EssenceOfDexterity)

  def essenceOfIntelligence: Card = mkCard("Essence of Intelligence", "Immediately increases Intelligence, only 1 essence may be played per turn", EssenceOfIntelligence)

  def essenceOfVitality: Card = mkCard("Essence of Vitality", "Immediately increases Dexterity, only 1 essence may be played per turn", EssenceOfVitality)
}

trait Magic extends CardBuilder {
  def healWounds: Card = mkCard("Heal Wounds", "Heals 30-60HP depending on level", HealWounds, charges = 2)

  def fireball: Card = mkCard("Fireball", "Deals fire damage and burns to all enemies", Fireball, 2)
}

trait Strength extends CardBuilder {
  def crushingBlow: Card = mkCard("Crushing Blow", "Deals heavy damage, but has an increased movement penalty", CrushingBlow, 4)
}

trait Dexterity extends CardBuilder {

  //  def tripleStrike: Card = mkCard("Triple Strike", "Attacks an enemy three times", TripleStrike)
}

trait Utility extends CardBuilder {

  // Not limited
  def rummage: Card = mkCard("Rummage", "Draw 2 cards", Rummage)


  def bagOfGold: Card = mkCard("Bag of Gold", "Gives 30 gold", BagOfGold)
}

trait CampFire extends CardBuilder {
  def rest: Card = mkCard("Rest", "Rest until you are fully recovered", Rest)

  def explore: Card = mkCard("Explore", "Draw until your hand is full (always draw at least two cards)", Explore)

  def restAndExplore: Card = mkCard("Rest and Explore", "Recover some health and draw two cards (or one if full)", RestAndExplore)

  def drop: Card = mkCard("Drop", "Discard an item by the Camp Fire", Drop)

  def destroy: Card = mkCard("Destroy", "Destroy an item in the fire", Destroy)

  def learnSkill: Card = mkCard("Learn Skill", "Permanently learn a skill, limited to 1 skill per 10 int", LearnSkill)
}

trait Equipment extends CardBuilder {
  def club: Card = mkEquip("Club", "Generic Club, minimal increase to damage",
    Club)

  def trollCrusher: Card = mkEquip("Troll Crusher", "Moderate increase to damage. Bonus damage based on the enemies current HP",
    TrollCrusher, Requirements(str = Some(22)))

  def leatherArmour: Card = mkEquip("Leather Armour", "Generic armour, slightly reduces damage taken",
    LeatherArmour, Requirements(str = Some(8)))

  def ringmail: Card = mkEquip("Ringmail", "Generic armour, slightly reduces damage taken",
    Ringmail, Requirements(str = Some(10)))

}


trait TreasureCards extends CardBuilder {

  def redCape: Card = mkEquip("Red Cape", "Shiny red cape, quick to equip, has a slight effect on damage taken",
    RedCape, Requirements(dex = Some(7)))

  def orcishArmour: Card = mkEquip("Orcish Armour", "Orc armour, greatly reduces damage taken",
    OrcishArmour, Requirements(str = Some(24)))

  def deceiver: Card = mkEquip("Renart's Deceiver",
    "Moderate damage, increased action speed, increased damage whilst on low life",
    RenartsDeceiver, Requirements(dex = Some(16)))

  def manamune: Card = mkEquip("Manamune",
    "Minimal increase to damage. Intellect heavily affects attack damage",
    Manamune, Requirements(str = Some(12), dex = Some(12)))

  def wandOfDefiance: Card = mkEquip("Wand of Defiance", "Minimal increase to damage, reduces damage taken",
    WandOfDefiance, Requirements(int = Some(15)))
}