package daibouken.free

sealed trait GameLoopStep[T]

object GameLoopSteps {

  case class ResetPlayerPosition() extends GameLoopStep[Unit]

  case class CompleteLoop[T]() extends GameLoopStep[T]

}
