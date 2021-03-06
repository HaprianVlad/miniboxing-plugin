package miniboxing.simple

class Members[@miniboxed T: Manifest](t: T, u: Int) {
  var a : T = t
  val b : Int = u

  def foo1(t1 : T) : T = t1
  def foo2(c : Int) : T = foo1(t)
  def foo3(c : Int): Int = foo4(a)
  def foo4(c : T): Int = b
  def hash() = t.hashCode
  def print() = println(t)
}


object Impl extends Members[Boolean](false, 1) {
  override def foo1(t: Boolean) : Boolean = true
}
