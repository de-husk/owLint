package owLint

import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.model.OWLOntology
import collection.JavaConversions._

// All linter tests should be defined in this object
object LinterTests {
  // ontology-must-have-version-info test
  def ontologyMustHaveVersionInfo (ontology: OWLOntology): LintFunctionResult = {
    val hasVersionInfo = OWLOntologyHasAnnotation(ontology, "http://www.w3.org/2002/07/owl#versionInfo")

    if (!hasVersionInfo) {
      return LintFunctionResult(false, List(OffendingInstance("AnnotationProperty", "http://www.w3.org/2002/07/owl#versionInfo")))
    }
    LintFunctionResult(true, List())
  }

  // ontology-must-have-dc-title
  def ontologyMustHaveDCTitle (ontology: OWLOntology): LintFunctionResult = {
    val hasDCTitle = OWLOntologyHasAnnotation(ontology, "http://purl.org/dc/elements/1.1/title")

    if (!hasDCTitle) {
      return LintFunctionResult(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/title")))
    }
    LintFunctionResult(true, List())
  }

  // ontology-must-have-dc-creator
  def ontologyMustHaveDCCreator (ontology: OWLOntology): LintFunctionResult = {
    val hasDCCreator = OWLOntologyHasAnnotation(ontology, "http://purl.org/dc/elements/1.1/creator")

    if (!hasDCCreator) {
      return LintFunctionResult(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/creator")))
    }
    LintFunctionResult(true, List())
  }

  // ontology-must-have-only-one-dc-creator
  def ontologyMustHaveOneDCCreator  (ontology: OWLOntology): LintFunctionResult = {
    val dcCreator: Option[OWLAnnotation] = ontology
      .getAnnotations
      .toList
      .find(a => a.getProperty.getIRI.toString == "http://purl.org/dc/elements/1.1/creator")
    
    val hasDCCreator = dcCreator match {
      case Some(t) => true
      case None => false    
    }

    if (hasDCCreator) {
      val hasMoreThanOneCreator = hasUnwantedDelimiters(dcCreator.get.getValue.toString)

      if (hasMoreThanOneCreator)
        return LintFunctionResult(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/creator")))
    }
    return LintFunctionResult(true, List())
  } 

  //ontology-must-have-only-one-dc-contributor
  def ontologyMustHaveOneDCContributor (ontology: OWLOntology): LintFunctionResult= {
    val dcContributor: Option[OWLAnnotation] = ontology
      .getAnnotations
      .toList
      .find(a => a.getProperty.getIRI.toString == "http://purl.org/dc/elements/1.1/contributor")

    val hasDCContributor = dcContributor match {
      case Some(t) => true
      case None => false    
    }

    if (hasDCContributor) {
      val hasMoreThanOneContributor = hasUnwantedDelimiters(dcContributor.get.getValue.toString)

      if (hasMoreThanOneContributor)
        return LintFunctionResult(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/contributor")))
    }
    LintFunctionResult(true, List())
  }

  //ontology-must-have-dc-date
  def ontologyMustHaveDCDate (ontology: OWLOntology): LintFunctionResult = {
    val hasDcDate = OWLOntologyHasAnnotation(ontology, "http://purl.org/dc/elements/1.1/date")
    if (!hasDcDate) {
      return LintFunctionResult(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/date")))
    }
    LintFunctionResult(true, List())
  }

  //iris-and-labels-are-unique
  def irisAndLabelsAreUnique (ontology: OWLOntology): LintFunctionResult= {
    val entities = getEntitiesDefinedInCurrentOWLFile(ontology)

    val iris = entities map { e =>
      e.getIRI.getFragment
    }
    
    val entitiesWithLabels = entities.filter(e => OWLEntityHasAnnotation(e, ontology, "rdfs:label")) 

    val labels = entitiesWithLabels map { e =>
      val label = e.getAnnotations(ontology).toList.find(a => a.getProperty.toString == "rdfs:label").get
      val pattern = """\"(.*)\"""".r
      pattern.findAllIn(label.getValue.toString).matchData.toList(0).group(1).trim
    }

    val matches =  iris.filter(labels.toSet) 

    if (matches.length != 0) {
      return LintFunctionResult(false, matches map { m =>
        OffendingInstance("IRI", ontology.getOntologyID.getOntologyIRI.toString+"#"+m)
      })
    }

    LintFunctionResult(true, List())
  }

  // entities-must-have-rdfs-comment
  def entitiesMustHaveRDFSComment (ontology: OWLOntology): LintFunctionResult = {
    val entities: List[OWLEntity] = getEntitiesDefinedInCurrentOWLFile(ontology)
    var offendingLines: List[OffendingInstance] = List[OffendingInstance]()
    
    entities foreach { entity =>
      val descriptionAnnotation = entity.getAnnotations(ontology).toList.find(a => a.getProperty.toString == "rdfs:comment")

      val hasDescription = descriptionAnnotation match {
        case Some(d) => true
        case None => false
      }

      if (!hasDescription) {
        offendingLines = offendingLines :+ OffendingInstance(entity.getEntityType.getName, entity.getIRI.toString)
      }
    }

    if (offendingLines.length != 0)
      return LintFunctionResult(false, offendingLines)

    LintFunctionResult(true, List())
  }

  //non-root-classes-need-genus-differentiation
  def nonRootClassesNeedGenusDifferentiation (ontology: OWLOntology): LintFunctionResult= {
    val classes = getClassesDefinedInCurrentOWLFile(ontology)
    var offendingLines: List[OffendingInstance] = List[OffendingInstance]()

    classes foreach { c =>
      //Check if current class is not a root node
      val superClasses = c.getSuperClasses(ontology).toList
      
      var hasGenusDifferentiation = false

      if (superClasses.length != 0) {
        // Check if it has genus-differentiation
        superClasses foreach { superClass =>
          val classExpressionType = superClass.getClassExpressionType

          if (classExpressionType != ClassExpressionType.OWL_CLASS) {
            hasGenusDifferentiation = true
          }
        }

        if (!hasGenusDifferentiation) {
          offendingLines = offendingLines :+ OffendingInstance(c.getEntityType.getName, c.getIRI.toString)
        }
      }
    }

    if (offendingLines.length != 0)
      return LintFunctionResult(false, offendingLines)

    LintFunctionResult(true, List())
  }

  //Linter helper functions
  def getClassesDefinedInCurrentOWLFile (ontology: OWLOntology): List[OWLClass] = {
    val classes: List[OWLClass] = ontology.getClassesInSignature.toList
    classes.filter(c => c.toString.contains(ontology.getOntologyID.getOntologyIRI.toString))
  }

 def getEntitiesDefinedInCurrentOWLFile (ontology: OWLOntology): List[OWLEntity] = {
   val entities: List[OWLEntity] = ontology.getSignature.toList
   entities.filter(e => e.toString.contains(ontology.getOntologyID.getOntologyIRI.toString))
 }

 def OWLEntityHasAnnotation(entity: OWLEntity, ontology:OWLOntology, iri: String): Boolean = {
    val annotation = entity
      .getAnnotations(ontology)
      .toList
      .find(a => a.getProperty.toString == iri.toString) //TODO: doesnt need a toString (just change parameter to IRI)

    annotation match {
      case Some(a) => true
      case None => false
    }
  }

 def OWLOntologyHasAnnotation(ontology:OWLOntology, iri: String): Boolean = {
    val annotation = ontology
      .getAnnotationPropertiesInSignature
      .toList
      .find(a => a.getIRI.toString == iri.toString) //TODO: doesnt need t a toString (Just change parameter to IRI)

    annotation match {
      case Some(a) => true
      case None => false
    }
  }

  def hasUnwantedDelimiters (test: String): Boolean = {
    val invalidCreatorReg = """(?i) and |\/|\n|_|\||\r\|\t|\v""".r

    val matches = invalidCreatorReg.findFirstMatchIn(test) match {
      case Some(m) => true
      case None => false
    }

    if (matches)
      return true

    false
  }
}
