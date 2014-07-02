package owLint;

import org.semanticweb.owlapi.model.OWLOntology

class OwLint (config: Map[String, Boolean]) {

  def doesFileLint (ontology: OWLOntology) : (Boolean, Array[OwLintError]) = { 
  



    (false, Array(OwLintError(22, "stupid logic"), OwLintError(19, "Entity without descriptions")))
  }




  case class OwLintError(
    //TODO: should this be more complex?
    // * example 1: lineNumber could hold array of all offending line numbers
    // * example 2: the actual text of the offending line from the file 
    lineNumber: Int = 0,
    problemDescription: String = ""
  )
}


