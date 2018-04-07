package chousen.game.actions

import java.util.UUID

import chousen.Optics.{CardsLens, MessagesLens, PlayerLens}
import chousen.api.data._
import chousen.util.CardsSyntax._
import chousen.util.LensUtil

object CardActionHandler extends ActionHandler {
  def handle(action: CardAction, cardId: Option[UUID]): (GameState) => GameState = {
    LensUtil.triLens(PlayerLens, CardsLens, MessagesLens).modify {
      case (p, cs, msgs) =>
        cardActions(action, cardId)(p, cs, msgs)
    }
  }

  private def cardActions(actionId: CardAction, cardId: Option[UUID]): (Player, Cards, Seq[GameMessage]) => (Player, Cards, Seq[GameMessage]) =
    actionId match {
      case Rummage => rummage
      case BagOfGold => bagOfGold
    }

  def rummage(p: Player, cs: Cards, msgs: Seq[GameMessage]): (Player, Cards, Seq[GameMessage]) = {
    val newCards = cs.drawNoLimit.drawNoLimit.drawNoLimit

    val foundCards = newCards.hand.filter(c => !cs.hand.contains(c))

    val targetMsg = GameMessage(s"${p.name} searches the area and finds: ${foundCards.map(_.name).mkString(", ")}")

    val gameMessages = msgs :+ targetMsg

    (p, newCards, gameMessages)
  }


  def bagOfGold(p: Player, cs: Cards, msgs: Seq[GameMessage]): (Player, Cards, Seq[GameMessage]) = {

    val gold = 30
    val m = GameMessage(s"${p.name} opens a bag of gold and finds $gold gold!")

    val gameMessages = msgs :+ m

    (p.copy(gold = p.gold + gold), cs, gameMessages)
  }

}
