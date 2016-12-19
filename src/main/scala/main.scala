/*
 Usage:
 *  This tool takes an optional argument, the path to the file to lint or to the folder
 containing the owl files in question.

 Use -h for more detail.
 */

package owLint

import scala.io.Source
import scala.util.matching.Regex
import java.nio.file.{Paths, Files}
import java.io.File
import spray.json._
import DefaultJsonProtocol._
import owLint.BuildInfo


import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology

object OwLintStarter {

  def main (args: Array[String]) = {
    deliverHelpTextIfNeeded(args)

    val lintingTarget = getLintingTargetFromArgs(args) match {
      case Some(c) => c
      case None => sys.exit(1)
    }

    var isFile = false

    if (isValidDirectory(lintingTarget)) {
      isFile = false
    } else if (isValidFile(lintingTarget)) {
      isFile = true
      if (!isValidOwlFile(lintingTarget)) {
        Console.err.println(Console.RED + "Error: " + lintingTarget + " is not an owl file!\nUse -h for help information." + Console.RESET)
        sys.exit(1)
      }
    } else {
      Console.err.println(Console.RED + "Error: " + lintingTarget + " is not a real file or directory!\nUse -h for help information." + Console.RESET)
      sys.exit(1)
    }

    val owLintConfig: Map[String, Boolean] = getOwLintConfig(lintingTarget, isFile)

    val owlFiles = isFile match {
      case true => Array(new File(lintingTarget))
      case false => getOwlFilesInCurrDirectory(lintingTarget)
    }

    if (owlFiles.length == 0) {
      Console.err.println(Console.RED + "Error: " + lintingTarget + " has no owl files!\nUse -h for help information." + Console.RESET)
      sys.exit(1)
    }

    val ontologyManager: OWLOntologyManager = OWLManager.createOWLOntologyManager
    val owLinter = new OwLint(owLintConfig)

    var allFilesPass = true

    owlFiles foreach { currentFile =>
      println(Console.YELLOW + "\n*** Processing " + currentFile.getName + " ***" + Console.RESET)

      val owlOntology: OWLOntology =  ontologyManager.loadOntologyFromOntologyDocument(currentFile)
      println("Loaded ontology:" + owlOntology)

      // Lint the currently loaded owl file
      val results = owLinter.doesFileLint(owlOntology)

      results foreach { result =>
        val passes = result.success
        val errors = result.errors
        if (passes == false) {
          allFilesPass = false
          println(Console.RED + "[error] " + result.name + Console.RESET)
          println(Console.RED + "\nLint Failed!" + Console.RESET)

          println(Console.MAGENTA+ "\n------------------------------" + Console.RESET)
          println(Console.RED + "Lint Error: " + Console.RESET)
          println(Console.RED + "\tReason For Failure:\t" + errors.problemDescription +  Console.RESET)
          println(Console.RED + "\tNumber of offenses:\t" + errors.offendingLines.length +  Console.RESET)

          errors.offendingLines foreach { line =>
            println(Console.GREEN + "\t[" + line.tyype + "] " + Console.RED  +"\t" + line.content +  Console.RESET)
          }
          println(Console.MAGENTA+ "------------------------------" + Console.RESET)

        } else {
          println("[" + Console.GREEN + "success" + Console.RESET + "] " + result.name  +   " successfully passed!" + Console.RESET)
        }
      }
    }

    if (!allFilesPass) {
      sys.exit(1)
    }
  }


  def deliverHelpTextIfNeeded (args: Array[String]) = {
    if (args.contains("-v") || args.contains("-version")) {
      println(Console.GREEN + "\n" + BuildInfo.name + " " + BuildInfo.version + Console.RESET)
      sys.exit(0)
    }

    if (args.contains("-h") || args.contains("-help")) {
      // Display help information
      println(Console.UNDERLINED + "owLint: OWL file linting tool " + Console.RESET)
      println(Console.CYAN + "This tool takes an optional argument, the path to the file to lint or to the folder containing the owl files in question." +  Console.RESET)
      println("Examples:" +
                Console.GREEN + "\n   $ owlint                             " + Console.YELLOW + "| Use current directory" +
                Console.GREEN + "\n   $ owlint owlFileFolder               " + Console.YELLOW + "| Use ./owlFileFolder as the directory" +
                Console.GREEN + "\n   $ owlint /home/username/somedir      " + Console.YELLOW + "| Use absolute path /home/username/somedir as the directory" +
                Console.GREEN + "\n   $ owlint someOwlFile.owl             " + Console.YELLOW + "| Only lint someOwlFile.owl (You can also use absolute paths for files)" +
                Console.GREEN + "\n   $ owlint -h | -help                  " + Console.YELLOW + "| Displays help information" +
                Console.RESET)
      sys.exit(0)
    }
  }


  def getLintingTargetFromArgs (args: Array[String]): Option[String] = {
    args.length match {
      case 0 => Some(System.getProperty("user.dir")) // Use current dir as linting target
      case 1 => Some(args(0))                        // Use inputted dir or file as linting target
      case moreThanOne => {                          // Return an error and exit because this tool only takes one argument
        Console.err.println(Console.RED + "Error: owLint only takes one argument!\nUse -h for help information." + Console.RESET)
        None
      }
    }
  }

  def isValidFile (file: String): Boolean = {
    val filePath = Paths.get(file)
    Files.exists(filePath) && Files.isRegularFile(filePath)
  }

  def isValidOwlFile (file: String): Boolean = {
    file.split("\\.")(1) == "owl"
  }

  def isValidDirectory (directory: String): Boolean = {
    val directoryPath = Paths.get(directory)
    Files.exists(directoryPath) && Files.isDirectory(directoryPath)
  }

  def getOwLintConfig (lintingTarget: String, isFile: Boolean): Map[String, Boolean]  = {
    val configPath = isFile match {
      case true => Paths.get(lintingTarget).getParent + "/.owlint"
      case false => lintingTarget + "/.owlint"
    }

    if (Files.exists(Paths.get(configPath))) {
      val localConfigFile = Source.fromFile(configPath).mkString
      val confJson = localConfigFile.parseJson
      val config = confJson.convertTo[Map[String, Boolean]]

      if (isOwLintConfigValid(config)) {
        return config
      } else {
        Console.err.println(Console.RED + "Error: .owlint file contains invalid options! Please check for errors!" + Console.RESET)
        sys.exit(1)
      }
    } else {
      return getDefaultConfig
    }
  }

  def isOwLintConfigValid (config: Map[String, Boolean]): Boolean = {
    val defaultConf = getDefaultConfig

    config.keys foreach { key =>
      val keyFound = defaultConf.keys.find(k => k == key) match {
        case Some(k) => true
        case  None => false
      }

      if (!keyFound)
        return false
    }
    true
  }

  def getDefaultConfig: Map[String, Boolean] = {
    Map (
      "ontology-must-have-version-info" -> true,
      "ontology-must-have-dc-title" -> true,
      "ontology-must-have-dc-creator" -> true,
      "ontology-must-have-only-one-dc-creator" -> true,
      "ontology-must-have-only-one-dc-contributor" -> true,
      "ontology-must-have-dc-date" -> true,
      "iris-and-labels-are-unique" -> true,
      "non-root-classes-need-genus-differentiation" -> true,
      "entities-must-have-rdfs-comment" -> true
    )
  }

  def getOwlFilesInCurrDirectory (currentDirectory: String): Array[File] = {
    val currFolder: File = new File(currentDirectory)
    val listOfFiles = currFolder.listFiles()
    val owlFileRegex = """.+\.owl""".r

    listOfFiles filter(f => owlFileRegex.pattern.matcher(f.getName).matches)
  }
}
