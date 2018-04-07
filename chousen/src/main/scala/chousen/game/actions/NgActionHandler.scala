package chousen.game.actions

import java.util.UUID

import chousen.Optics
import chousen.Optics.{CardsLens, CurrentEncounterLens, MessagesLens, PlayerLens}
import chousen.api.data.{Battle, _}
import chousen.game.core.turn.PostTurnOps
import chousen.util.LensUtil
import monocle.Lens
import monocle.macros.GenLens

case class NDungeon(current: Battle, next: Option[Battle])

case class NState(player: Player, cards: Cards, dungeon: NDungeon, messages: Seq[GameMessage])

trait NgActionHandler {
  val NextLens: Lens[GameState, Seq[Battle]] = GenLens[GameState](_.dungeon.remainingEncounters)

  def handle(action: CardAction, cardId: Option[UUID]): (GameState) => GameState = {
    gs =>

    LensUtil.quintLens(PlayerLens, CardsLens, CurrentEncounterLens, NextLens, MessagesLens).modify {
      case (p, cs, e, n, m) =>
        val ns = NState(p, cs, NDungeon(e, n.headOption), m)


        (ns.player, ns.cards, ns.dungeon.current, ns.dungeon.next.toSeq ++ n.tail, ns.messages)
    }

//
//    LensUtil.triLens(PlayerLens, CardsLens, MessagesLens).modify {
//      case (p, cs, msgs) =>
//        cardActions(action, cardId)(p, cs, msgs)
//    }


    def handleDead: (GameState) => GameState =
      Optics.EncounterLens.modify(PostTurnOps.handleDead)

      gs
  }
}



