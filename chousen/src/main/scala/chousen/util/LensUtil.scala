package chousen.util

import monocle.Lens


object LensUtil {

  def quintLens[S, A, B, C, D, E]
  (lsa: Lens[S, A], lsb: Lens[S, B], lsc: Lens[S, C], lsd: Lens[S, D], lse: Lens[S, E]): Lens[S, (A, B, C, D, E)] =
    Lens.apply[S, (A, B, C, D, E)](s =>
      (lsa.get(s), lsb.get(s), lsc.get(s), lsd.get(s), lse.get(s))) {
      t =>
        lsa.set(t._1)
          .andThen(lsb.set(t._2))
          .andThen(lsc.set(t._3))
          .andThen(lsd.set(t._4))
          .andThen(lse.set(t._5))
    }

  def triLens[S, A, B, C](lsa: Lens[S, A], lsb: Lens[S, B], lsc: Lens[S, C]): Lens[S, (A, B, C)] =
    Lens.apply[S, (A, B, C)](s => (lsa.get(s), lsb.get(s), lsc.get(s)))(t => lsa.set(t._1).andThen(lsb.set(t._2)).andThen(lsc.set(t._3)))

  //Traversal[Set[Enemy], Enemy]

  def duoLens[S, A, B](lsa: Lens[S, A], lsb: Lens[S, B]): Lens[S, (A, B)] =
    Lens.apply[S, (A, B)](s => (lsa.get(s), lsb.get(s)))(t => lsa.set(t._1).andThen(lsb.set(t._2)))
}
