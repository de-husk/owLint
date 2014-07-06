package owLint;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology
import collection.JavaConversions._

class OwLint (config: Map[String, Boolean]) {

  //TODO: change CurrentLint to Lint and put OwLintError inside of it
  def doesFileLint (ontology: OWLOntology) : List[(CurrentLint, OwLintErrors)] = { 
    var resultList: List[(CurrentLint, OwLintErrors)] = List[(CurrentLint, OwLintErrors)]()

    config foreach { conf =>
      if (conf._2 == true) {
        val lintTesterRunner = lintTestMappings.find({case (a, b) => a == conf._1}).get

        //Run the lintTest Function
        val lintResults = lintTesterRunner._2._1(ontology)

        if (!lintResults._1) {
          //This test fails
          resultList = resultList :+ (CurrentLint(false, conf._1), OwLintErrors(lintTesterRunner._2._2, lintResults._2))
        } else {
          //This test passes
          resultList = resultList :+ (CurrentLint(true, conf._1), OwLintErrors())
        }
      }
    }
    resultList
  }

  val lintTestMappings: Map [String, Tuple2[Function1[OWLOntology, (Boolean, List[OffendingInstance])], String]] = 
    Map (
      "entities-must-have-descriptions" -> Tuple2(entitiesMustHaveDescriptions, "All entities must have description attributes."),
      "ontology-must-have-version-info" -> Tuple2(ontologyMustHaveVersionInfo, "The ontology must have a version info annotation."),
      "ontology-must-have-dc-title" -> Tuple2(ontologyMustHaveDCTitle, "The ontology must have a DC title annotation"),
      "ontology-must-have-dc-creator" -> Tuple2(ontologyMustHaveDCCreator, "The ontology must have a DC creator annotation")
    )

  case class CurrentLint (
    success: Boolean,
    name: String
  )

  case class OwLintErrors (
    problemDescription: String = "",
    offendingLines: List[OffendingInstance] = List[OffendingInstance]()
  )

  case class OffendingInstance (
    tyype: String,  // TODO: Or do I want the actual OWL EntityType
    content: String
  )


  // linting tests live below:
  //Return:  (didItLint, List[OffendingInstance])


  // ontology-must-have-version-info test
  def ontologyMustHaveVersionInfo (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val annotations = ontology.getAnnotationPropertiesInSignature.toArray.toList
    val versionInfo = annotations.filter(a => a.toString  == "owl:versionInfo")

    if (versionInfo.length == 0) {
      return (false, List(OffendingInstance("ANNOTATION_PROPERTY", "owl:versionInfo")))
    }
    (true, List())
  }

  // ontology-must-have-dc-title
  def ontologyMustHaveDCTitle (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val annotations = ontology.getAnnotationPropertiesInSignature.toArray.toList
    val versionInfo = annotations.filter(a => a.toString  ==  "<http://purl.org/dc/elements/1.1/title>")

    if (versionInfo.length == 0) {
      return (false, List(OffendingInstance("ANNOTATION_PROPERTY", "http://purl.org/dc/elements/1.1/title")))
    }
    (true, List())
  }

  // ontology-must-have-dc-creator
  def ontologyMustHaveDCCreator (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val annotations = ontology.getAnnotationPropertiesInSignature.toArray.toList
    val versionInfo = annotations.filter(a => a.toString  ==  "<http://purl.org/dc/elements/1.1/creator>")

    if (versionInfo.length == 0) {
      return (false, List(OffendingInstance("ANNOTATION_PROPERTY", "http://purl.org/dc/elements/1.1/creator")))
    }
    (true, List())
  }




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


