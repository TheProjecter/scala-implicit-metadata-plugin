package ee.scala.plugin.implicitMetadata
import java.lang.reflect.Type

case class Metadata(
        val name:String, 
        val definition:Type,
        val owner:Any
) {}

object Metadata {
     implicit def any2Metadata(any:Any):Metadata = {
        try {
        	any.asInstanceOf[Metadata]
        } catch {
            case e => throw new RuntimeException("Make sure you have the 'ee.implicitMetadataPlugin' compiler plugin installed", e)
        }
    }
}