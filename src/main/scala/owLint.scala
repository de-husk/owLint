package owLint;

import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.model.OWLOntology
import collection.JavaConversions._

class OwLint (config: Map[String, Boolean]) {

  def doesFileLint (ontology: OWLOntology) : List[LintResult] = { 
    var resultList: List[LintResult] = List[LintResult]()

    config foreach { conf =>
      if (conf._2 == true) {
        val lintTesterRunner = lintTestMappings.find({case (a, b) => a == conf._1}).get

        //Run the lintTest Function
        val lintResults = lintTesterRunner._2.function(ontology)

        if (!lintResults._1) {
          //This test fails
          val sortedResults = sortErrorsAlphabetically(lintResults._2)
          resultList = resultList :+ LintResult(false, conf._1, OwLintErrors(lintTesterRunner._2.description, sortedResults))
        } else {
          //This test passes
          resultList = resultList :+ LintResult(true, conf._1, OwLintErrors())
        }
      }
    }
    resultList
  }

  def sortErrorsAlphabetically (errors: List[OffendingInstance]): List[OffendingInstance] = {
    //Sort the errors by type, and content strings
    errors.sortBy(error => (error.tyype, error.content))
  }


  val lintTestMappings: Map [String, LintFunctionDef] = 
    Map (
      "entities-must-have-rdfs-comment" -> LintFunctionDef(entitiesMustHaveRDFSComment, "All entities must have rdfs:comment attribute."),
      "ontology-must-have-version-info" -> LintFunctionDef(ontologyMustHaveVersionInfo, "The ontology must have a version info annotation."),
      "ontology-must-have-dc-title" -> LintFunctionDef(ontologyMustHaveDCTitle, "The ontology must have a DC title annotation"),
      "ontology-must-have-dc-creator" -> LintFunctionDef(ontologyMustHaveDCCreator, "The ontology must have a DC creator annotation"),
      "ontology-must-have-only-one-dc-creator" -> LintFunctionDef(ontologyMustHaveOneDCCreator, "The ontology cannot have more than one DC creator listed in the dc:creator annotation."),
      "ontology-must-have-only-one-dc-contributor" -> LintFunctionDef(ontologyMustHaveOneDCContributor, "The ontology cannot have more than one DC contributor in each dc:contributor annotation")
    )

  case class LintResult (
    success: Boolean,
    name: String,
    errors: OwLintErrors
  )

  case class OwLintErrors (
    problemDescription: String = "",
    offendingLines: List[OffendingInstance] = List[OffendingInstance]()
  )

  case class OffendingInstance (
    tyype: String,  // TODO: Or do I want the actual OWL EntityType
    content: String
  )

  case class LintFunctionDef (
    function: Function1[OWLOntology, (Boolean, List[OffendingInstance])],
    description: String
  )


  // linting tests live below:
  //Return:  (didItLint, List[OffendingInstance])

  //TODO: DRY up this code
  //TODO: Better way to grab a single annotation via IRI?

  // ontology-must-have-version-info test
  def ontologyMustHaveVersionInfo (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val versionInfo = ontology
      .getAnnotationPropertiesInSignature
      .toList
      .find(a => a.getIRI.toString == "http://www.w3.org/2002/07/owl#versionInfo")
    
    val hasVersionInfo = versionInfo match {
      case Some(v) => true
      case None => false    
    }

    if (!hasVersionInfo) {
      return (false, List(OffendingInstance("AnnotationProperty", "http://www.w3.org/2002/07/owl#versionInfo")))
    }
    (true, List())
  }

  // ontology-must-have-dc-title
  def ontologyMustHaveDCTitle (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val dcTitle = ontology
      .getAnnotationPropertiesInSignature
      .toList
      .find(a => a.getIRI.toString == "http://purl.org/dc/elements/1.1/title")
    
    val hasDCTitle = dcTitle match {
      case Some(t) => true
      case None => false    
    }

    if (!hasDCTitle) {
      return (false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/title")))
    }
    (true, List())
  }

  // ontology-must-have-dc-creator
  def ontologyMustHaveDCCreator (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val dcCreator = ontology
      .getAnnotationPropertiesInSignature
      .toList
      .find(a => a.getIRI.toString == "http://purl.org/dc/elements/1.1/creator")
    
    val hasDCCreator = dcCreator match {
      case Some(t) => true
      case None => false    
    }

    if (!hasDCCreator) {
      return (false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/creator")))
    }
    (true, List())
  }

  // ontology-must-have-only-one-dc-creator
  def ontologyMustHaveOneDCCreator  (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val dcCreator: Option[OWLAnnotation] = ontology
      .getAnnotations
      .toList
      .find(a => a.getProperty.getIRI.toString == "http://purl.org/dc/elements/1.1/creator")
    
    val hasDCCreator = dcCreator match {
      case Some(t) => true
      case None => false    
    }

    if (hasDCCreator) {
      val creatorNames = dcCreator.get.getValue

      val invalidCreatorReg = """(?i) and |\/|\n|_|\||\r\|\t|\v""".r

      val matches = invalidCreatorReg.findFirstMatchIn(creatorNames.toString) match {
        case Some(m) => true
        case None => false
      }

      if (matches)
        return (false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/creator")))
    }
    return (true, List())
  } 

  //ontology-must-have-only-one-dc-contributor
  def ontologyMustHaveOneDCContributor (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val dcContributor: Option[OWLAnnotation] = ontology
      .getAnnotations
      .toList
      .find(a => a.getProperty.getIRI.toString == "http://purl.org/dc/elements/1.1/contributor")

    val hasDCContributor = dcContributor match {
      case Some(t) => true
      case None => false    
    }

    if (hasDCContributor) {
      //TODO: DRY BELOW
      val contributorNames = dcContributor.get.getValue

      val invalidCreatorReg = """(?i) and |\/|\n|_|\||\r\|\t|\v""".r

      val matches = invalidCreatorReg.findFirstMatchIn(contributorNames.toString) match {
        case Some(m) => true
        case None => false
      }

      if (matches)
        return (false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/contributor")))

    }
    (true, List())
  }




  // entities-must-have-rdfs-comment
  def entitiesMustHaveRDFSComment (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {
    val classes: List[OWLEntity] = ontology.getClassesInSignature.toList
    val individuals: List[OWLEntity] = ontology.getIndividualsInSignature.toList

    //properties = objectProperties + annotationProperties + dataProperties
    val objectProperties: List[OWLEntity] = ontology.getObjectPropertiesInSignature.toList
    val annotationProperties: List[OWLEntity] = ontology.getAnnotationPropertiesInSignature.toList
    val dataProperties: List[OWLEntity] = ontology.getDataPropertiesInSignature.toList

    val properties: List[OWLEntity] =  objectProperties ++ annotationProperties ++ dataProperties

    val entities: List[OWLEntity] = classes ++ individuals ++ properties
   
    var offendingLines: List[OffendingInstance] = List[OffendingInstance]()
    
    entities foreach { entity =>
      if (entity.toString.contains(ontology.getOntologyID.getOntologyIRI.toString)) {
        // if current entity is defined in the current ontology
        val descriptionAnnotation = entity.getAnnotations(ontology).toList.find(a => a.getProperty.toString == "rdfs:comment")

        val hasDescription = descriptionAnnotation match {
          case Some(d) => true
          case None => false
        }

        if (!hasDescription) {
          offendingLines = offendingLines :+ OffendingInstance(entity.getEntityType.getName, entity.getIRI.toString)
        }
      }
    }

    if (offendingLines.length != 0)
      return (false, offendingLines)

    (true, List())
  }

}


