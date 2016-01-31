import org.scalatest._
import owLint._
import sys.process._

class owLintIntegrationTest extends FlatSpec with Matchers {

  "owLint" should "return success exit code when file successfully lints" in {
    val pseq = Seq("sbt", "run test/passing_files")
    val pb = scala.sys.process.Process(pseq)
    val exitCode = pb.!

    assert(exitCode == 0)
  }

  it should "return success exit code when help menu is displayed" in {
    val pseq = Seq("sbt", "run -h")
    val pb = scala.sys.process.Process(pseq)
    val exitCode = pb.!

    assert(exitCode == 0)
  }

  it should "return error exit code when an invalid directory is inputted" in {
      val pseq = Seq("sbt", "run totally_not_real/batz")
      val pb = scala.sys.process.Process(pseq)
      val exitCode = pb.!

      assert(exitCode == 1)
  }

  it should "return error exit code when a file fails linting" in {
    val pseq = Seq("sbt", "run test/failing_files")
    val pb = scala.sys.process.Process(pseq)
    val exitCode = pb.!

    assert(exitCode == 1)
  }

}



