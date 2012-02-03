import ee.scala.plugin.implicitMetadata.Metadata
object TestImplicitMetadataPlugin {
    
    val val_a_string = ""
    val val_b_int = 0
    val val_c_metadata:Metadata = new Metadata("not implicit Metadata", classOf[Metadata], null)
    val val_d_list_string = List[String]()
    
    val subInstance = new {
        val val_e_string_subInstance_owner = ""
    }
    
    var var_f_string = ""
    def def_g = {}
    def def_h() = {}
    
	def main(args: Array[String]) = {
	    test(val_a_string);
	    test(val_b_int)
	    test(val_c_metadata)
	    test(val_d_list_string)
	    test(subInstance.val_e_string_subInstance_owner)
	    test(var_f_string)
	    test(def_g)
	    test(def_h)
	    
	    val_a_string test_: this
	    //test(TestImplicitMetadataPlugin) //throws a compiler error 
	    
	}

    def test(metadata:Metadata) = {
        metadata.owner match {
            case TestImplicitMetadataPlugin => println("TestImplicitMetadataPlugin")
            case other => println(if (other == subInstance) "subInstance" else "unknown")
            
        } 
        
        println(metadata)
    }
    
    def test_:(metadata:Metadata) = test(metadata)
}