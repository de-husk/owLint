package owLint

import org.semanticweb.owlapi.model.OWLOntology

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
    errors.sortBy(error => (error.tyype, error.content))
  }

  val lintTestMappings: Map [String, LintFunctionDef] = 
    Map (
      "entities-must-have-rdfs-comment" -> LintFunctionDef(LinterTests.entitiesMustHaveRDFSComment, "All entities must have rdfs:comment attribute."),
      "ontology-must-have-version-info" -> LintFunctionDef(LinterTests.ontologyMustHaveVersionInfo, "The ontology must have a version info annotation."),
      "ontology-must-have-dc-title" -> LintFunctionDef(LinterTests.ontologyMustHaveDCTitle, "The ontology must have a DC title annotation"),
      "ontology-must-have-dc-creator" -> LintFunctionDef(LinterTests.ontologyMustHaveDCCreator, "The ontology must have a DC creator annotation"),
      "ontology-must-have-only-one-dc-creator" -> LintFunctionDef(LinterTests.ontologyMustHaveOneDCCreator, "The ontology cannot have more than one DC creator listed in the dc:creator annotation."),
      "ontology-must-have-only-one-dc-contributor" -> LintFunctionDef(LinterTests.ontologyMustHaveOneDCContributor, "The ontology cannot have more than one DC contributor in each dc:contributor annotation"),
      "iris-and-labels-are-unique" -> LintFunctionDef(LinterTests.irisAndLabelsAreUnique, "The human readable portion of IRIs and rdfs:labels must be a unique set"),
      "ontology-must-have-dc-date" -> LintFunctionDef(LinterTests.ontologyMustHaveDCDate, "The ontology must have a dc:date annotation")
    )
}

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
