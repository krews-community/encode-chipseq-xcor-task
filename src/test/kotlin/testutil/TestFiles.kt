package testutil
import java.nio.file.*

fun getResourcePath(relativePath: String): Path {

    val url = TestCmdRunner::class.java.classLoader.getResource(relativePath)
     return Paths.get(url.toURI())
}

// Resource Directories
val testInputResourcesDir = getResourcePath("test-input-files")
//val testOutputResourcesDir = getResourcePath("test-output-files")


// Test Working Directories
val testDir = Paths.get("/tmp/chipseq-test")!!
val testInputDir = testDir.resolve("input")!!
val testOutputDir = testDir.resolve("output")!!


//val TA = testInputDir.resolve("control1_align_output.nodup.tagAlign.gz")
val TASE = testInputDir.resolve("outputnofiltse.tagAlign.gz") //signal end
val TAPE = testInputDir.resolve("outputnofiltpe.tagAlign.gz") //paired end

val ENCODETA = testInputDir.resolve("ENCFF000ASP.tagAlign.gz")