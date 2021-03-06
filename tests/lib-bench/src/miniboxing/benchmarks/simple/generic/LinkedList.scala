package miniboxing.benchmarks.simple.generic

// Function
trait Function1[-T, +S] {
  def apply(t: T): S
}



// Tuple
case class Tuple2[+T1, +T2](_1: T1, _2: T2) {
  override def toString() = "(" + _1.toString + "," + _2.toString + ")"
}



// Builder
trait Builder[-T, +To] {

  def +=(e1: T): Unit
  def finalise: To
}

class ListBuilder[T] extends Builder[T, List[T]] {

  private var head: List[T] = Nil

  def +=(e1: T): Unit = {
    if (head == Nil) head = new ::(e1, Nil)
    else head = new ::(e1, head)
  }

  def finalise: List[T] = head.reverse
}



// Numeric
trait Numeric[T] {
  def plus(x: T, y: T): T
  def zero: T
}



// Traversable
trait Traversable[+T] {

  def mapTo[U, To](f: Function1[T, U])(b: Builder[U, To]): To = {
    val buff = b

    foreach(new Function1[T,Unit] { def apply(t: T): Unit = buff += f(t) })

    buff.finalise
  }

  def map[U](f: Function1[T, U]): List[U] = mapTo[U, List[U]](f)(new ListBuilder)

  def sum[B >: T](implicit n : Numeric[B]): B = {
    var buff = n.zero

    foreach(new Function1[B,Unit] { def apply(b: B): Unit = buff = n.plus(buff,b) })

    buff
  }

  def foreach[U](f: Function1[T, U]): Unit
}



// Iterable
trait Iterable[+T] extends Traversable[T] {
  def iterator: Iterator[T]

  def zipTo[U, To](that: Iterable[U])(b: Builder[Tuple2[T, U], To]): To = {
    val buff = b
    val these = this.iterator
    val those = that.iterator

    while (these.hasNext() && those.hasNext()) {
      buff += new Tuple2[T,U](these.next(),those.next())
    }

    buff.finalise
  }

  def zip[U](that: Iterable[U]): List[Tuple2[T, U]] = zipTo[U, List[Tuple2[T, U]]](that)(new ListBuilder[Tuple2[T, U]])
}



// Iterator
trait Iterator[+T] {
  def hasNext(): Boolean
  def next(): T
}



// List
abstract class List[+T] extends Iterable[T] {

  def iterator = new Iterator[T] {
    var current = List.this
    def hasNext = current != Nil
    def next() = {
      val t = current.head
      current = current.tail
      t
    }
  }
  def foreach[U](f: Function1[T, U]) {
    val it = iterator
    while (it.hasNext) f(it.next())
  }

  def head: T
  def tail: List[T]
  def size: Int

  def ::[S >: T](e1: S) : List[S] = new ::[S](e1, this)

  def reverse: List[T] = {
    val it = iterator
    var list: List[T] = Nil
    while (it.hasNext) list = it.next :: list
    list
  }
}

case class ::[T](head: T, tail: List[T]) extends List[T] {
  def size = 1 + tail.size

  override def toString = head.toString + " :: " + tail.toString
}

case object Nil extends List[Nothing] {
  def head = throw new NoSuchElementException("head of empty list")
  def tail = throw new NoSuchElementException("tail of empty list")

  def size = 0

  override def toString = "Nil"
}

