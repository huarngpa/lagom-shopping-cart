package app.huarngpa.shoppingcart.impl.helpers

object CollectionImplicits {

  implicit class ListImprovements[+T](val list: List[T]) {

    def dropFirstMatch[A >: T](value: A): List[A] = {
      val index = list.indexOf(value)
      if (index < 0) list
      else if (index == 0) list.tail
      else {
        val (a, b) = list.splitAt(index)
        a ++ b.tail
      }
    }
  }
}
