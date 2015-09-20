**In development**

A compiler plugin that provides some information that would only be available at compile time.

An example:

```
object X {
	val y:String = "z"
	
	test(y)
	
	def test(metadata:Metadata) = {
	    println(metadata)
	}
}
```

The Metadata class:

```
case class Metadata(
        val name:String, 
        val definition:Type,
        val owner:Any
) {}
```