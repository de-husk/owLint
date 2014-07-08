import org.scalatest._

class MainTestsSpec extends FlatSpec with Matchers {
  "maths" should "work" in {
    assert(1 + 1 == 2)
  }
}
