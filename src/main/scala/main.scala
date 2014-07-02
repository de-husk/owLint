/*
 Usage:
 *  This tool takes an optional argument, the absolute or relative path to the folder containing
    the owl files in question.
*/



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

    println(currentDirectory)

  }
}



/* TODO : 
 *  This file is the entry point of the tool.

 *  This file will:
 1. Grab .owLint file in currentDirectory and override default configurations if the file exists.
 2. For each OWL file in currentDirectory:
     *  parse the OWL file with the OWL API 
     *  pass the owl api file object (OWLOntology) and the config object into the constructor and get the returned owLintResponse object that has true/false for if the lint was successful and then an array of errors if it wasn't
*/
