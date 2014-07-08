import org.scalatest._

class MainTestsSpec extends FlatSpec with Matchers {

  "getCurrentDirectoy" should "return current directory if none is specified" in {
    val fakeArgs = Array[String]()
    val currentDirectory = owLintStarter.getCurrentDirectory(fakeArgs).get
    assert(currentDirectory == System.getProperty("user.dir"))
  } 

  it should "return the inputted argument if there is only one inputted argument" in {
    val argument = "test"
    val fakeArgs = Array(argument)
    val currentDirectory = owLintStarter.getCurrentDirectory(fakeArgs).get
    assert(currentDirectory == argument)
  }

  it should "return None if inputted more than one argument" in {
    val fakeArgs = Array("wut", "I", "doing?")
    val currentDirectory = owLintStarter.getCurrentDirectory(fakeArgs)
    assert(currentDirectory == None)
  }


  "isValidDirectoryPath" should "return true for a valid directory" in {
    val validDirectoryPath = "./test"
    val isValidDirectoryPath = owLintStarter.isValidDirectoryPath(validDirectoryPath)
    assert(isValidDirectoryPath == true)
  }

  it should "return false for a valid file path to a non-folder" in {
    val filePath = "./README.md"
    val isValidDirectoryPath = owLintStarter.isValidDirectoryPath(filePath)
    assert(isValidDirectoryPath == false)
  }

  it should "return false for an incorrect file path" in {
    val invalidPath = "./assdasdadasdasdasd"
    val isValidDirectoryPath = owLintStarter.isValidDirectoryPath(invalidPath)
    assert(isValidDirectoryPath == false)
  }
}
