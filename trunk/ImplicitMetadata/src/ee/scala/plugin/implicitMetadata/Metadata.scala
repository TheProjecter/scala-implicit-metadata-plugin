package ee.scala.plugin.implicitMetadata

case class Metadata(
        val name:String, 
        val definition:Class[_],
        val owner:Any
) {}

object Metadata {
     implicit def any2Metadata(any:Any):Metadata = {
        try {
        	any.asInstanceOf[Metadata]
        } catch {
            case _ => throw new RuntimeException("Make sure you have the 'ee.implicitMetadataPlugin' compiler plugin installed")
        }
    }
}