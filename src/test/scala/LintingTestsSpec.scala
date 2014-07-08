import org.scalatest.{FlatSpec, Matchers}

class LintingTestsSpec extends FlatSpec with Matchers{
  "maths" should "work" in {
    assert(1 + 1 == 2)
  }

}
