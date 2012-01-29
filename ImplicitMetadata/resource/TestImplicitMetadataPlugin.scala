import ee.scala.plugin.implicitMetadata.Metadata
object TestImplicitMetadataPlugin {
    
    val val_a_string = ""
    val val_b_int = 0
    val val_c_metadata:Metadata = new Metadata("not implicit Metadata", classOf[Metadata])
    val val_d_list_string = List[String]()
    
	def main(args: Array[String]) = {
	    test(val_a_string);
	    test(val_b_int)
	    test(val_c_metadata)
	    test(val_d_list_string)
	}

    def test(metadata:Metadata) = {
        println(metadata)
    }
}