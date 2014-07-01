


object owLintStarter {
  def main (args: Array[String]) = println("todo")
}



/* TODO : 
 *  This file is the entry point of the tool.

 *  This file will:
 1. Grab .owLint file in currentDirectory and override default configurations if the file exists.
 2. For each OWL file in currentDirectory:
     *  parse the OWL file with the OWL API 
     *  pass the owl api file object (TODO: what is this specifically?) and the config object into the constructor and get the returned owLintResponse object that has true/false for if the lint was successful and then an array of errors if it wasn't
*/
