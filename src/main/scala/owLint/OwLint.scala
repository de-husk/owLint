package owLint

import org.semanticweb.owlapi.model.OWLOntology

class OwLint(config: Map[String, Boolean]) {

  def doesFileLint(ontology: OWLOntology) : List[LintResult] = { 
    var resultList: List[LintResult] = List[LintResult]()

    config foreach { case (lintName, runTest) =>
      if (runTest) {
        val lintTesterRunner = lintTestMappings.find({case (a, b) => a == lintName}).get

        //Run the lintTest Function
        val lintResults = lintTesterRunner._2.f(ontology)

        if (!lintResults.success) {
          val sortedResults = sortErrorsAlphabetically(lintResults.offendingInstances)
          resultList = resultList :+ LintResult(false, lintName, OwLintErrors(lintTesterRunner._2.description, sortedResults))
        } else {
          resultList = resultList :+ LintResult(true, lintName, OwLintErrors())
        }
      }
    }
    resultList
  }

  def sortErrorsAlphabetically (errors: List[OffendingInstance]): List[OffendingInstance] = {
    errors.sortBy(error => (error.tyype, error.content))
  }

  val lintTestMappings: Map [String, LintF] =
    Map (
      "entities-must-have-rdfs-comment" -> LintF(
        LinterTests.entitiesMustHaveRDFSComment,
        "All entities must have rdfs:comment attribute."
      ),

      "ontology-must-have-version-info" -> LintF(
        LinterTests.ontologyMustHaveVersionInfo,
        "The ontology must have a version info annotation."
      ),

      "ontology-must-have-dc-title" -> LintF(
        LinterTests.ontologyMustHaveDCTitle,
        "The ontology must have a DC title annotation"
      ),

      "ontology-must-have-dc-creator" -> LintF(
        LinterTests.ontologyMustHaveDCCreator,
        "The ontology must have a DC creator annotation"
      ),

      "ontology-must-have-only-one-dc-creator" -> LintF(
        LinterTests.ontologyMustHaveOneDCCreator,
        "The ontology cannot have more than one DC creator listed in the dc:creator annotation."
      ),

      "ontology-must-have-only-one-dc-contributor" -> LintF(
        LinterTests.ontologyMustHaveOneDCContributor,
        "The ontology cannot have more than one DC contributor in each dc:contributor annotation"
      ),

      "iris-and-labels-are-unique" -> LintF(
        LinterTests.irisAndLabelsAreUnique,
        "The human readable portion of IRIs and rdfs:labels must be a unique set"
      ),

      "ontology-must-have-dc-date" -> LintF(
        LinterTests.ontologyMustHaveDCDate,
        "The ontology must have a dc:date annotation"
      ),

      "non-root-classes-need-genus-differentiation" -> LintF(
        LinterTests.nonRootClassesNeedGenusDifferentiation,
        """All non-root classes (Each owl:class with an rdfs:subclassOf statement)
           need to have a statement differentiating between other owl:classes that
           are also subclasses of the same parent owl:class. An example genus
           differentating statement takes the form:
           rdfs:subClassOf <Object Property> <Quantifier> <named owl:class or anonymous class>
        """)
    )
}

case class LintResult(
  success: Boolean,
  name: String,
  errors: OwLintErrors
)

case class OwLintErrors(
  problemDescription: String = "",
  offendingLines: List[OffendingInstance] = List[OffendingInstance]()
)

case class OffendingInstance(
 tyype: String,  // TODO: Or do I want the actual OWL EntityType
 content: String
)

case class LintF(
  f: OWLOntology => LintR,
  description: String
)

case class LintR(
  success: Boolean,
  offendingInstances: List[OffendingInstance]
)
