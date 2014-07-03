package owLint;

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
          errors = errors :+ OwLintError(lintTesterRunner._2._2, lintResults._2)
        }
      }
    }
    (passes, errors)
  }

  val lintTestMappings: Map [String, Tuple2[Function1[OWLOntology, (Boolean, List[OffendingInstance])], String]] = 
    Map (
      "entities-must-have-descriptions" -> Tuple2(entitiesMustHaveDescriptions, "All entities must have description attributes.")
    )


  case class OwLintError (
    problemDescription: String = "",
    offendingLines: List[OffendingInstance] = List[OffendingInstance]()
  )

  case class OffendingInstance (
    tyype: String,  // TODO: Or do I want the actual OWL EntityType
    content: String
  )


  // linting tests live below:
  //Return:  (didItLint, List[OffendingInstance])

  def entitiesMustHaveDescriptions (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    //TODO: change from classes to all entities
    val classes = ontology.getClassesInSignature.iterator
    var offendingLines: List[OffendingInstance] = List()
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
        }
      }

      if (!hasSeenDescription) {
        val tyype = clazz.getEntityType.getName
        offendingLines = offendingLines :+ OffendingInstance(tyype, clazz.getIRI.toString)
        passes = false
      }
    }
    (passes, offendingLines)
  }

}


