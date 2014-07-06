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


object owLintStarter {
  def main (args: Array[String]) = {

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

    val currentDirectory = args.length match {
      case 0 => System.getProperty("user.dir") // Use current dir as currentDirectory
      case 1 => args(0)                        // Use inputted dir as currentDirectory
      case moreThanOne => {                    // Return an error and exit because this tool only takes one argument
        Console.err.println(Console.RED + "Error: owLint only takes one argument!\nUse -h for help information." + Console.RESET)
        sys.exit(1)
      }
    }

    // Make sure that currentDirectory exists
    if (!Files.exists(Paths.get(currentDirectory))) {
      Console.err.println(Console.RED + "Error: " + currentDirectory + " is not a real directory!\nUse -h for help information." + Console.RESET)
      sys.exit(1)
    }

    // Read in .owlint config if the file exists
    val configPath = currentDirectory + "/.owlint" 
    var owLintConfig: Map[String, Boolean] = Map()

    if (Files.exists(Paths.get(configPath))) {
      val localConfigFile = Source.fromFile(configPath).mkString
      val confJson = localConfigFile.parseJson
      owLintConfig = confJson.convertTo[Map[String, Boolean]]
    } else {
      owLintConfig = getDefaultConfig
    }

    // Process each *.owl file in currentDirectory
    val currFolder: File = new File(currentDirectory)
    val listOfFiles = currFolder.listFiles()
    val owlFileRegex = """.+\.owl""".r

    val owlFiles =  listOfFiles filter(f => owlFileRegex.pattern.matcher(f.getName).matches)

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

      val results = owLinter.doesFileLint(owlOntology)

      results foreach { result =>
        val passes = result._1.success
        val errors = result._2
        if (passes == false) {
          Console.println(Console.RED + "\nLint Failed!!" + Console.RESET)

          Console.err.println(Console.MAGENTA+ "\n------------------------------" + Console.RESET)
          Console.err.println(Console.RED + "Lint Error: " + Console.RESET)
          Console.err.println(Console.RED + "\tReason For Failure:\t" + errors.problemDescription +  Console.RESET)
          Console.err.println(Console.RED + "\tNumber of offenses:\t" + errors.offendingLines.length +  Console.RESET)

          errors.offendingLines foreach { line =>
            Console.err.println(Console.GREEN + "\t[" + line.tyype + "] " + Console.RED  + line.content +  Console.RESET)
          }
            Console.err.println(Console.MAGENTA+ "------------------------------" + Console.RESET)

        } else {
          println("[" + Console.GREEN + "success" + Console.RESET + "] " + result._1.name  +   " successfully passed!" + Console.RESET)
        }
      }
    }
  }

  def getDefaultConfig : Map[String, Boolean] = {
    // Default settings is all checks are true
     Map (
       "ontology-must-have-version-info" -> true, 
       "ontology-must-have-dc-title" -> true,
       "ontology-must-have-dc-creator" -> true
    )
  }

}
