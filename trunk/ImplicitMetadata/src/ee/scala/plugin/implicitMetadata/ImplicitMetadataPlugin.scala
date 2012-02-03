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
	        
            //grab the name of the metadata class
	        lazy val metadataClassName = classOf[Metadata].getName

	        //grab the actual representations within the compiler space to the class and object (module)
	        lazy val metadataModule = definitions.getModule(metadataClassName)
	        lazy val metadataClass = definitions.getClass(metadataClassName)
	        
	        //grab the method
	        lazy val any2MetadataMethod = definitions.getMember(metadataModule, "any2Metadata")
	        
	        var index = Map[Symbol, ValDef]()
	        
	        override def transform(tree: Tree): Tree = {
	        	tree match {
	        		//find calls to the any2Metadata method
		        	case methodFound @ Apply(fun, List(arg)) if fun.symbol == any2MetadataMethod => {
		        	    //replace the call with a new metadata instance 
		        	    typedPos(tree.pos) { newMetadataInstance(arg) }
		        	}
		        	case r:ValDef => {
		        		//store value references, they are needed for right associative calls
		        		index += (r.symbol -> r)
		        		super.transform(r)
		        	}
		        	case x =>
		        	    //anything else
		        	    super.transform(x)
		        }
	        	
	        }
	        
	        /**
	         * Constructs a new instance of Metadata based on the argument passed to the 
	         * any2Metadata method.
	         */
	        def newMetadataInstance(arg:Tree) = {
	            //make a call
	            Apply(
	                //select the constructor of the metadata class
	        		Select(
	        			New(
	        				TypeTree(metadataClass.tpe)
			        	),
			        	nme.CONSTRUCTOR
	        		),
	        		//we need the select from the arg
		        	arg match {
	        	        case select:Select => getArgumentList(select)
	        	        //we get an Ident when a right associative operator is used
	        	        case ident:Ident => {
	        	            index.get(ident.symbol) match {
	        	                //match the definition to get at the select
	        	                case Some(ValDef(_, _, _, select:Select)) => getArgumentList(select)
	        	                case x => unsupportedConversion(arg)
	        	            }
	        	        }
	        	        //in the case of a partially applied methods, the select is nested in the anonymous method
	        	        case Apply(select:Select, _) => getArgumentList(select)
	        	        case x => unsupportedConversion(arg)
	        	    }
			    )
	        }
	        
	        /**
	         * Create the argument list for the constructor of Metadata based on the given Select
	         */
	        def getArgumentList(select:Select) = {
	        	List(
	        		Literal(select.name.toString),
	        		select.tpe match {
	        			//In case of a method we do not have a type
	        		    case methodType:MethodType => Literal(Constant(null)) 
	        		    case x => Literal(x)
	        		},
	        		select.qualifier
    			)
	        }
	        
	        /**
	         * Used to report an error
	         */
	        def unsupportedConversion(tree:Tree):List[Tree] = {
	            compilationUnit.error(tree.pos, "Can not convert to Metadata, only var, val and def members can be converted")
	        	List(
            		Literal("Unknown arg '" + tree + "' of type '" + tree.getClass.getSimpleName),
		        	Literal(tree.tpe),
		        	Literal(Constant(null))
			    )
	        }	        
	        
	    }
    }


}