package owLint;

//TODO: refactor some of these tuples into case classes

import org.semanticweb.owlapi.model.OWLOntology

class OwLint (config: Map[String, Boolean]) {

  def doesFileLint (ontology: OWLOntology) : (Boolean, Array[OwLintError]) = { 
    var passes = true
    var errors: Array[OwLintError] = Array[OwLintError]()

    config foreach { conf =>
      if (conf._2 == true) {
        val lintTesterRunner = lintTestMappings.find({case (a, b) => a == conf._1}).get

        //Run the lintTest Function
        val lintResults = lintTesterRunner._2._1(ontology)

        if (!lintResults._1) {
          //This test fails
          passes = false
          errors = errors :+ OwLintError(0, lintTesterRunner._2._2, lintResults._2)
        }

      }
    }
    (passes, errors)
  }

  val lintTestMappings: Map [String, Tuple2[Function1[OWLOntology, (Boolean, List[(String, String)])], String]] = 
    Map (
      "entities-must-have-descriptions" -> Tuple2(entitiesMustHaveDescriptions, "All entites must have description attributes.")
    )


  case class OwLintError(
    lineNumber: Int = 0,
    problemDescription: String = "",
    offendingLine: List[(String, String)] = List[(String, String)]()
  )




  // linting tests live below:
  // TODO: put these in a different class?

  //Return:  (didItLint, List[(OWLEntityType, offendingLine)])
  def entitiesMustHaveDescriptions (ontology: OWLOntology): (Boolean, List[(String, String)]) = {
    //TODO: change from classes to all entities
    val classes = ontology.getClassesInSignature.iterator
    var offendingLines: List[(String, String)] = List()
    var passes = true

    while (classes.hasNext) {
      val clazz = classes.next

      // Check if entity has description attribute
      val annotations = clazz.getAnnotations(ontology).iterator

      var hasSeenDescription = false

      while (annotations.hasNext) {
        val a = annotations.next

        if (a.getProperty.toString == "rdf:Description") {
          hasSeenDescription = true

          println(clazz.isOWLClass)
        }
      }

      if (!hasSeenDescription) {
        val tyype = clazz.getEntityType.getName
        offendingLines = offendingLines :+ (tyype, clazz.getIRI.toString)
        passes = false
      }
    }
    (passes, offendingLines)
  }

}


