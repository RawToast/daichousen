package chousen.game.cards

import chousen.api.data.Card
import org.scalatest.WordSpec

class CardCatalogueSpec extends WordSpec {

  "Card Catalogue" should {

    val catalogue = CardCatalogue


    "Provide methods to create cards" in {
      val card1 = catalogue.deceiver
      val card2 = catalogue.redCape
      val card3 = catalogue.rummage
      val card4 = catalogue.bagOfGold

      assert(card1.name == "Renart's Deceiver")
      assert(Set(card1.action, card2.action, card3.action, card4.action).size == 4)
    }

    "Contain pre-made decks" that {
      val deck1 = catalogue.fighterDeck

      standardAssertions("Fighter", deck1)
    }


    def standardAssertions(name: String, deck: Seq[Card]) = {
      s"$name deck has less than 60 cards" in { // For now, not enough choice for 60
        val size = deck.size
        assert(size <= 60)
      }
    }

  }

}
