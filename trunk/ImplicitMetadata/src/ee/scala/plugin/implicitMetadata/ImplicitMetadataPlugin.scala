package ee.scala.plugin.implicitMetadata

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.Plugin
import scala.collection.immutable.List
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.transform.Transform
import scala.tools.nsc.transform.TypingTransformers

class ImplicitMetadataPlugin(val global:Global) extends Plugin {
    
    val name = "ee.implicitMetadataPlugin"
	val description = """
		A plugin that implicitly converts any argument passed to a method to metadata.
	    
		For example:
	    
		class X {
			val y:String = "z"
			
			test(y)
			
			def test(metadata:Metadata) = {
	    		println(metadata)
			}
		}
	"""
	val components = List[PluginComponent](ImplicitMetadataPluginComponent)

	private object ImplicitMetadataPluginComponent extends PluginComponent with Transform with TypingTransformers {
    	
        val global = ImplicitMetadataPlugin.this.global
        val phaseName = ImplicitMetadataPlugin.this.name
	    val runsAfter:List[String] = List("typer")
	    
	    import global._
        
	    protected def newTransformer(compilationUnit:CompilationUnit): Transformer = new TypingTransformer(compilationUnit) {
	        
	        lazy val metadataClassName = classOf[Metadata].getName
	        lazy val metadataModule = definitions.getModule(metadataClassName)
	        lazy val metadataClass = definitions.getClass(metadataClassName)
	        
	        lazy val any2MetadataMethod = definitions.getMember(metadataModule, "any2Metadata")
	        
	        override def transform(tree: Tree): Tree = {
	        	tree match {
		        	case methodFound @ Apply(fun, List(arg:Select)) if fun.symbol == any2MetadataMethod =>
			          typedPos(tree.pos) { newMetadataInstance(arg) }
		        	case x => super.transform(x)
		        }   
	        }
	      
	        def newMetadataInstance(arg:Select) = 
	        	Apply(
	        		Select(
	        			New(
	        				TypeTree(metadataClass.tpe)
			        	),
			        	nme.CONSTRUCTOR
	        		),
			        List(
			        	Literal(arg.symbol.name.toString),
			        	Literal(arg.symbol.info.typeOfThis),
			        	arg.qualifier
			        )
			    )
	        
	    }
    }


}