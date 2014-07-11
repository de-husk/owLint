/*
 Usage:
 *  This tool takes an optional argument, the absolute or relative path to the folder containing
    the owl files in question. Use -h for more detail.
*/

import owLint._

import scala.io.Source
import scala.util.matching.Regex
import java.nio.file.{Paths, Files}
import java.io.File
import spray.json._
import DefaultJsonProtocol._ 

import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology

object OwLintStarter {

  def main (args: Array[String]) = {
    deliverHelpTextIfNeeded(args)
    
    val currentDirectory = getCurrentDirectory(args) match { 
      case Some(c) => c
      case None => sys.exit(1)
    }

    if (!isValidDirectoryPath(currentDirectory)) {
      Console.err.println(Console.RED + "Error: " + currentDirectory + " is not a real directory!\nUse -h for help information." + Console.RESET)
      sys.exit(1)
    }

    val owLintConfig: Map[String, Boolean] = getOwLintConfig(currentDirectory)

    // Process each *.owl file in currentDirectory
    val owlFiles = getOwlFilesInCurrDirectory(currentDirectory)

    if (owlFiles.length == 0) {
      Console.err.println(Console.RED + "Error: " + currentDirectory + " has no owl files!\nUse -h for help information." + Console.RESET)
      sys.exit(1)
    }

    val ontologyManager: OWLOntologyManager = OWLManager.createOWLOntologyManager
    val owLinter = new OwLint(owLintConfig)

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
          Console.println(Console.RED + "\nLint Failed!!" + Console.RESET)

          Console.err.println(Console.MAGENTA+ "\n------------------------------" + Console.RESET)
          Console.err.println(Console.RED + "Lint Error: " + Console.RESET)
          Console.err.println(Console.RED + "\tReason For Failure:\t" + errors.problemDescription +  Console.RESET)
          Console.err.println(Console.RED + "\tNumber of offenses:\t" + errors.offendingLines.length +  Console.RESET)

          errors.offendingLines foreach { line =>
            Console.err.println(Console.GREEN + "\t[" + line.tyype + "] " + Console.RED  +"\t" + line.content +  Console.RESET)
          }
            Console.err.println(Console.MAGENTA+ "------------------------------" + Console.RESET)

        } else {
          println("[" + Console.GREEN + "success" + Console.RESET + "] " + result.name  +   " successfully passed!" + Console.RESET)
        }
      }
    }
  }

 
  def deliverHelpTextIfNeeded (args: Array[String]) = {
    if (args.contains("-h") || args.contains("-help")) {
      // Display help information
      println(Console.UNDERLINED + "owLint: OWL file linting tool " + Console.RESET)
      println(Console.CYAN + "This tool takes an optional argument, the absolute or relative path to the folder containing the owl files in question." +  Console.RESET)
      println("Examples:" +
        Console.GREEN + "\n   $ owlint                             " + Console.YELLOW + "| Use current directory" +
        Console.GREEN + "\n   $ owlint owlFileFolder               " + Console.YELLOW + "| Use ./owlFileFolder as the directory" +
        Console.GREEN + "\n   $ owlint /home/username/somedir      " + Console.YELLOW + "| Use absolute path /home/username/somedir as the directory" +
        Console.GREEN + "\n   $ owlint -h | -help                  " + Console.YELLOW + "| Displays help information" +
        Console.RESET)
      sys.exit(0)
    }
  }


  def getCurrentDirectory (args: Array[String]): Option[String] = {
    args.length match {
      case 0 => Some(System.getProperty("user.dir")) // Use current dir as currentDirectory
      case 1 => Some(args(0))                        // Use inputted dir as currentDirectory
      case moreThanOne => {                          // Return an error and exit because this tool only takes one argument
        Console.err.println(Console.RED + "Error: owLint only takes one argument!\nUse -h for help information." + Console.RESET)
        None
      }
    }
  }

  def isValidDirectoryPath (currentDirectory: String): Boolean = {
    Files.exists(Paths.get(currentDirectory)) && Files.isDirectory(Paths.get(currentDirectory))
  }

  def getOwLintConfig (currentDirectory: String): Map[String, Boolean]  = {
    // Read in .owlint config if the file exists
    val configPath = currentDirectory + "/.owlint"

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
