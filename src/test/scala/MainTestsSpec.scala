import org.scalatest._
import owLint._

class MainTestsSpec extends FlatSpec with Matchers {

  "getCurrentDirectoy" should "return current directory if none is specified" in {
    val fakeArgs = Array[String]()
    val currentDirectory = OwLintStarter.getLintingTargetFromArgs(fakeArgs).get
    assert(currentDirectory == System.getProperty("user.dir"))
  }

  it should "return the inputted argument if there is only one inputted argument" in {
    val argument = "test"
    val fakeArgs = Array(argument)
    val currentDirectory = OwLintStarter.getLintingTargetFromArgs(fakeArgs).get
    assert(currentDirectory == argument)
  }

  it should "return None if inputted more than one argument" in {
    val fakeArgs = Array("wut", "I", "doing?")
    val currentDirectory = OwLintStarter.getLintingTargetFromArgs(fakeArgs)
    assert(currentDirectory == None)
  }

  "isValidDirectoryPath" should "return true for a valid directory" in {
    val validDirectoryPath = "./test"
    val isValidDirectoryPath = OwLintStarter.isValidDirectory(validDirectoryPath)
    assert(isValidDirectoryPath == true)
  }

  it should "return false for an incorrect file path" in {
    val invalidPath = "./assdasdadasdasdasd"
    val isValidDirectoryPath = OwLintStarter.isValidDirectory(invalidPath)
    assert(isValidDirectoryPath == false)
  }

  // TODO: Add tests for:
  // * isValidFile()
  // * getOwLintConfig()
  // * getOwlFilesInCurrDirectory()

  "isOWLintConfigValid" should "return true with valid config file" in {
    val validConfig: Map[String, Boolean] = Map (
      "ontology-must-have-version-info" -> true, 
      "ontology-must-have-dc-title" -> false
    )
    assert(OwLintStarter.isOwLintConfigValid(validConfig))
  }

  it should "return false with invalid config file (containing invalid option keys)" in {
    val validConfig: Map[String, Boolean] = Map (
      "totally-not-a-right-option" -> true, 
      "ontology-must-have-dc-title" -> false
    )
    assert(!OwLintStarter.isOwLintConfigValid(validConfig))
  }

}
