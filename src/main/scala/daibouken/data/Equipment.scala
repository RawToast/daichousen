package daibouken.data

case class Equipment(weapon: Option[Weapon], armour: Option[Armour])

case class Weapon(name: String, dmg: Int, requirements: Requirements, effects: Seq[WeaponEffect])
sealed trait WeaponEffect

case class Armour(name: String, defense: Int, requirements: Requirements)