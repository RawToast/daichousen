package chousen

import chousen.api.types.{EqualityInstances, EqualitySyntax}
import chousen.util.GameStateOps

object Implicits extends Instances with Syntax
object ChousenImplicits extends ChousenImplicits

trait ChousenImplicits extends GameStateOps

trait Instances extends EqualityInstances

trait Syntax extends EqualitySyntax