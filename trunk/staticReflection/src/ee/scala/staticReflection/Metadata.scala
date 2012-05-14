package ee.scala.staticReflection

import language.experimental.macros
import scala.reflect.makro.Context
import language.implicitConversions

case class Metadata(instance: AnyRef, name: String) {
  import scala.reflect.mirror

  lazy val symbol = mirror.typeOfInstance(instance).member(mirror.newTermName(name))

  def value = mirror.invoke(instance, symbol)()
}

object Metadata extends ((AnyRef, String) => Metadata) {

  implicit def anyToMetadata(sym: Any): Metadata = macro MetadataMacro.anyToMetadataImpl

}

private[staticReflection] object MetadataMacro {

  def anyToMetadataImpl(c: Context)(sym: c.Expr[Any]): c.Expr[Metadata] = {
    import c.mirror._

    def createMetadataInstance(select: Select):Tree = gen.mkMethodCall(
      staticModule("ee.scala.staticReflection.Metadata"),
      newTermName("apply"),
      List(select.qualifier, Literal(Constant(select.name.toString))))

    val metadata = sym.tree match {
      //normal select
      case select: Select => createMetadataInstance(select)
      
      //could be a call using a right associative operator
      case Ident(name) => 
        c.enclosingMethod.collect {
          case ValDef(_, refName, _, select:Select) if refName == name => createMetadataInstance(select)
        }
        .headOption
        .getOrElse(throw new Exception("Could not find ValDef for " + name))
        
      case _ => throw new Exception("Could not create metadata")
      
    }

    Expr(metadata)
  }
}