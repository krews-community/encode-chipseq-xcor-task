import org.junit.jupiter.api.*
import step.*
import testutil.*
import testutil.cmdRunner
import testutil.setupTest
import org.assertj.core.api.Assertions.*

class XcorTests {
    @BeforeEach fun setup() = setupTest()
    @AfterEach fun cleanup() = cleanupTest()

     @Test fun `run xcor SE  `() {
     cmdRunner.xcor(ENCODETA.toString(),"chrM",15000000,-1,-500,null,2,"histone",false,"xcoropt", testOutputDir)
      assertThat(testOutputDir.resolve("xcoropt.cc.fraglen.txt")).exists()

    }
   @Disabled @Test fun `run xcor PE  `() {
        cmdRunner.xcor(TAPE.toString(),"chrM",0,1,-500,null,1,"histone",true,"xcoropt", testOutputDir)
       assertThat(testOutputDir.resolve("xcoropt.cc.fraglen.txt")).exists()

   }

}