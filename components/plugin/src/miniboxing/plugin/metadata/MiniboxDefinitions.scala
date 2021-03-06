package miniboxing.plugin
package metadata

import scala.tools.nsc.plugins.PluginComponent
import scala.collection.immutable.ListMap
import miniboxing.runtime.MiniboxConstants._

trait MiniboxDefinitions {
  this: PluginComponent =>

  import global._
  import definitions._
  import miniboxing.runtime.MiniboxConstants._

  lazy val MinispecClass = rootMirror.getRequiredClass("scala.miniboxed")
  /**
   * This class should only appear in the tree during the `minibox` phase
   * and should be cleaned up afterwards, during the `minibox-cleanup` phase.
   */
  lazy val StorageClass = {
    // This is what is should look like:
    // ```
    //   package __root__.scala {
    //     class storage extends Annotation with TypeConstraint
    //   }
    // ```
    val AnnotationName = "scala.annotation.Annotation"
    val TypeConstrName = "scala.annotation.TypeConstraint"
    val AnnotationTpe = rootMirror.getRequiredClass(AnnotationName).tpe
    val TypeConstrTpe = rootMirror.getRequiredClass(TypeConstrName).tpe

    val StorageName = newTypeName("storage")
    val StorageSym = ScalaPackageClass.newClassSymbol(StorageName, NoPosition, 0L)
    StorageSym setInfoAndEnter ClassInfoType(List(AnnotationTpe, TypeConstrTpe), newScope, StorageSym)
    StorageSym
  }


  // array ops
  lazy val MiniboxArrayObjectSymbol = rootMirror.getRequiredModule("miniboxing.runtime.MiniboxArray")
  lazy val mbarray_update = definitions.getMember(MiniboxArrayObjectSymbol, newTermName("mbarray_update_minibox"))
  lazy val mbarray_apply  = definitions.getMember(MiniboxArrayObjectSymbol, newTermName("mbarray_apply_minibox"))
  lazy val mbarray_length = definitions.getMember(MiniboxArrayObjectSymbol, newTermName("mbarray_length"))
  lazy val mbarray_new    = definitions.getMember(MiniboxArrayObjectSymbol, newTermName("mbarray_new"))

  // Any ops
  lazy val TagDipatchObjectSymbol = rootMirror.getRequiredModule("miniboxing.runtime.MiniboxDispatch")
  lazy val tag_hashCode = definitions.getMember(TagDipatchObjectSymbol, newTermName("mboxed_hashCode"))
  lazy val other_==     = definitions.getMember(TagDipatchObjectSymbol, newTermName("mboxed_eqeq_other"))
  lazy val tag_==       = definitions.getMember(TagDipatchObjectSymbol, newTermName("mboxed_eqeq_tag"))
  lazy val notag_==     = definitions.getMember(TagDipatchObjectSymbol, newTermName("mboxed_eqeq_notag"))
  lazy val tag_toString = definitions.getMember(TagDipatchObjectSymbol, newTermName("mboxed_toString"))

  // conversions
  lazy val ConversionsObjectSymbol = rootMirror.getRequiredModule("miniboxing.runtime.MiniboxConversions")
  // type tag conversions
  lazy val minibox2box        = definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2box"))
  lazy val box2minibox        = definitions.getMember(ConversionsObjectSymbol, newTermName("box2minibox_tt"))

  // artificially created marker methods
  lazy val marker_minibox2box =
    newPolyMethod(1, ConversionsObjectSymbol, newTermName("marker_minibox2box"), 0L)(
      tpar => (Some(List(tpar.head.tpeHK withAnnotation AnnotationInfo(StorageClass.tpe, Nil, Nil))), tpar.head.tpeHK))
  lazy val marker_box2minibox =
    newPolyMethod(1, ConversionsObjectSymbol, newTermName("marker_box2minibox"), 0L)(
      tpar => (Some(List(tpar.head.tpeHK)), tpar.head.tpeHK withAnnotation AnnotationInfo(StorageClass.tpe, Nil, Nil)))

  // direct conversions
  lazy val x2minibox = Map[Symbol, Symbol](
      // Unit is incompatible with the Java-based runtime
      // UnitClass ->    definitions.getMember(ConversionsObjectSymbol, newTermName("unit2minibox")),
      BooleanClass -> definitions.getMember(ConversionsObjectSymbol, newTermName("boolean2minibox")),
      ByteClass ->    definitions.getMember(ConversionsObjectSymbol, newTermName("byte2minibox")),
      CharClass ->    definitions.getMember(ConversionsObjectSymbol, newTermName("char2minibox")),
      ShortClass ->   definitions.getMember(ConversionsObjectSymbol, newTermName("short2minibox")),
      IntClass ->     definitions.getMember(ConversionsObjectSymbol, newTermName("int2minibox")),
      LongClass ->    definitions.getMember(ConversionsObjectSymbol, newTermName("long2minibox")),
      DoubleClass ->  definitions.getMember(ConversionsObjectSymbol, newTermName("double2minibox")),
      FloatClass ->   definitions.getMember(ConversionsObjectSymbol, newTermName("float2minibox"))
    )
  lazy val minibox2x = Map[Symbol, Symbol](
      // Unit is incompatible with the Java-based runtime
      // UnitClass ->    definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2unit")),
      BooleanClass -> definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2boolean")),
      ByteClass ->    definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2byte")),
      CharClass ->    definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2char")),
      ShortClass ->   definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2short")),
      IntClass ->     definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2int")),
      LongClass ->    definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2long")),
      DoubleClass ->  definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2double")),
      FloatClass ->   definitions.getMember(ConversionsObjectSymbol, newTermName("minibox2float"))
    )

  lazy val standardTypeTagTrees = Map(
      UnitClass ->    Literal(Constant(UNIT)),
      BooleanClass -> Literal(Constant(BOOLEAN)),
      ByteClass ->    Literal(Constant(BYTE)),
      CharClass ->    Literal(Constant(CHAR)),
      ShortClass ->   Literal(Constant(SHORT)),
      IntClass ->     Literal(Constant(INT)),
      LongClass ->    Literal(Constant(LONG)),
      DoubleClass ->  Literal(Constant(DOUBLE)),
      FloatClass ->   Literal(Constant(FLOAT)),
      NothingClass -> Literal(Constant(REFERENCE))
    )

  // Manifest's newArray
  lazy val Manifest_newArray = definitions.getMember(FullManifestClass, newTermName("newArray"))

  // TODO: This will also take the storage type
  def storageType(tparam: Symbol): Type =
    tparam.tpe.withAnnotations(List(Annotation.apply(StorageClass.tpe, Nil, ListMap.empty)))

  lazy val opStorageClass = Map(
    mbarray_apply -> LongClass,
    mbarray_update -> LongClass,
    mbarray_length -> LongClass,
    mbarray_new -> LongClass,
    tag_hashCode -> LongClass,
    tag_toString -> LongClass,
    tag_== -> LongClass,
    notag_== -> LongClass,
    other_== -> LongClass
  )
}
