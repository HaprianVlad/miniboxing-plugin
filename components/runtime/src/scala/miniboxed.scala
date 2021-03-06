/**
 * An annotation to signal miniboxing-based specialization. Notice that the
 * miniboxing compiler plugin needs to be available to scalac, otherwise your
 * code won't be transformed
 */
package scala
import annotation.{Annotation, TypeConstraint}

/**
 * This annotation, used in code transformed by the miniboxing compiler
 * plugin, will result in a lightweight specialization that gives good
 * speedups while keeping the bytecode size from exploding.
 *
 * @see The [[http://scala-miniboxing.org]] website.
 */
class miniboxed extends annotation.StaticAnnotation
