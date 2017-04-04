package owLint

import org.semanticweb.owlapi.model.{OWLOntology, _}

import scala.collection.JavaConversions._

// Put all new tests in this object.
object LinterTests {
  def ontologyMustHaveVersionInfo(ontology: OWLOntology): LintR =
    if (ontologyHasAnnotation(ontology, IRI.create("http://www.w3.org/2002/07/owl#versionInfo")))
      LintR(true, List())
    else
      LintR(false, List(OffendingInstance("AnnotationProperty", "http://www.w3.org/2002/07/owl#versionInfo")))

  def ontologyMustHaveDCTitle(ontology: OWLOntology): LintR =
    if (ontologyHasAnnotation(ontology, IRI.create("http://purl.org/dc/elements/1.1/title")))
      LintR(true, List())
    else
      LintR(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/title")))

  def ontologyMustHaveDCCreator(ontology: OWLOntology): LintR =
     if (ontologyHasAnnotation(ontology, IRI.create("http://purl.org/dc/elements/1.1/creator")))
       LintR(true, List())
     else
       LintR(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/creator")))

  def ontologyMustHaveOneDCCreator(ontology: OWLOntology): LintR = {
    val c = ontology
      .getAnnotations
      .find(a => a.getProperty.getIRI.toString == "http://purl.org/dc/elements/1.1/creator")

    c match {
      case Some(x) if hasUnwantedDelimiters(x) =>
        LintR(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/creator")))
      case _ =>
        LintR(true, List())
    }
  }

  def ontologyMustHaveOneDCContributor(ontology: OWLOntology): LintR= {
    val c = ontology
      .getAnnotations
      .find(a => a.getProperty.getIRI.toString == "http://purl.org/dc/elements/1.1/contributor")

    c match {
      case Some(x) if hasUnwantedDelimiters(x) =>
        LintR(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/contributor")))
      case _ =>
        LintR(true, List())
    }
  }

  def ontologyMustHaveDCDate(ontology: OWLOntology): LintR = {
    val hasDcDate = ontologyHasAnnotation(ontology, IRI.create("http://purl.org/dc/elements/1.1/date"))
    if (!hasDcDate) {
      return LintR(false, List(OffendingInstance("AnnotationProperty", "http://purl.org/dc/elements/1.1/date")))
    }
    LintR(true, List())
  }

  def irisAndLabelsAreUnique(ontology: OWLOntology): LintR = {
    val entities = getEntitiesDefinedInCurrentOWLFile(ontology)

    val iris = entities map { e =>
      e.getIRI.getFragment
    }

    val entitiesWithLabels = entities.filter(e => entityHasAnnotation(e, ontology, "rdfs:label"))

    val labels = entitiesWithLabels map { e =>
      val label = e
        .getAnnotations(ontology)
        .find(a => a.getProperty.toString == "rdfs:label")
        .get
      val pattern = """\"(.*)\"""".r
      pattern.findAllIn(label.getValue.toString).matchData.toList(0).group(1).trim
    }

    val matches = iris.filter(labels.toSet)

    if (matches.nonEmpty) {
      return LintR(false, matches.map({ m =>
        OffendingInstance("IRI", ontology.getOntologyID.getOntologyIRI.toString+"#"+m)
      }))
    }

    LintR(true, List())
  }

  def entitiesMustHaveRDFSComment (ontology: OWLOntology): LintR = {
    val offendingLines = getEntitiesDefinedInCurrentOWLFile(ontology) flatMap { e =>
      val c = e
        .getAnnotations(ontology)
        .find(a => a.getProperty.toString == "rdfs:comment")

      c match {
        case Some(x) => None
        case None => Some(OffendingInstance(e.getEntityType.getName, e.getIRI.toString))
      }
    }

    if (offendingLines.nonEmpty)
      LintR(false, offendingLines)
    else
      LintR(true, List())
  }

  def nonRootClassesNeedGenusDifferentiation (ontology: OWLOntology): LintR= {
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
      return LintR(false, offendingLines)

    LintR(true, List())
  }

  // OWL helper functions for linting tests:

  def getClassesDefinedInCurrentOWLFile(ontology: OWLOntology): List[OWLClass] = {
    val classes: List[OWLClass] = ontology.getClassesInSignature.toList
    classes.filter(c => c.toString.contains(ontology.getOntologyID.getOntologyIRI.toString))
  }

 def getEntitiesDefinedInCurrentOWLFile(ontology: OWLOntology): List[OWLEntity] = {
   val entities: List[OWLEntity] = ontology.getSignature.toList
   entities.filter(e => e.toString.contains(ontology.getOntologyID.getOntologyIRI.toString))
 }

 def entityHasAnnotation(entity: OWLEntity, ontology:OWLOntology, prop: String): Boolean = {
   entity
     .getAnnotations(ontology)
     .toList
     .find(a => a.getProperty.toString == prop)
     match {
       case Some(a) => true
       case None => false
    }
  }

 def ontologyHasAnnotation(ontology:OWLOntology, iri: IRI): Boolean = {
     ontology
       .getAnnotationPropertiesInSignature
       .toList
       .find(a => a.getIRI.equals(iri))
       match {
         case Some(a) => true
         case None => false
     }
  }

  def hasUnwantedDelimiters(anno: OWLAnnotation): Boolean = {
    val invalidCreatorReg = """(?i) and |\/|\n|_|\||\r\|\t|\v""".r
    invalidCreatorReg.findFirstMatchIn(anno.getValue.toString) match {
      case Some(m) => true
      case None => false
    }
  }
}
