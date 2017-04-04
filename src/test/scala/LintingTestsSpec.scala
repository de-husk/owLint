import org.scalatest.{FlatSpec, Matchers}
import java.io.File
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology

import owLint._

class LintingTestsSpec extends FlatSpec with Matchers{
  val ontologyManager: OWLOntologyManager = OWLManager.createOWLOntologyManager

  val passingOntology: OWLOntology = ontologyManager.loadOntologyFromOntologyDocument(new File("./test/pizza.owl"))
  val failingOntology: OWLOntology = ontologyManager.loadOntologyFromOntologyDocument(new File("./test/pizza-fails.owl"))
  val creatorFailingOntology: OWLOntology = ontologyManager.loadOntologyFromOntologyDocument(new File("./test/pizza-more-than-one-creator-fails.owl"))
  val genusDiffPassingOntology: OWLOntology = ontologyManager.loadOntologyFromOntologyDocument(new File("./test/genus-diff.owl"))

  "ontology-must-have-version-info" should "return true on proper test owl file" in {
    val result = LinterTests.ontologyMustHaveVersionInfo(passingOntology)
    assert(result.success)
    assert(result.offendingInstances.length == 0)
  }

  it should "return false on failing test owl file" in {
    val result = LinterTests.ontologyMustHaveVersionInfo(failingOntology)
    assert(!result.success)
    assert(result.offendingInstances.length != 0)
  }

  "ontology-must-have-dc-title" should "return true on proper test owl file" in {
    val result = LinterTests.ontologyMustHaveDCTitle(passingOntology)
    assert(result.success)
    assert(result.offendingInstances.length == 0)
  }

  it should "return false on failing test owl file" in {
    val result = LinterTests.ontologyMustHaveDCTitle(failingOntology)
    assert(!result.success)
    assert(result.offendingInstances.length != 0)
  }

  "ontology-must-have-dc-creator" should "return true on valid test owl file" in {
    val result = LinterTests.ontologyMustHaveDCCreator(passingOntology)
    assert(result.success)
    assert(result.offendingInstances.length == 0)
  }

  it should "return false on invalid test owl file" in {
    val result = LinterTests.ontologyMustHaveDCCreator(failingOntology)
    assert(!result.success)
    assert(result.offendingInstances.length != 0)
  }

  "ontology-must-have-only-one-dc-creator" should "return true on proper test owl file" in {
    val result = LinterTests.ontologyMustHaveOneDCCreator(passingOntology)
    assert(result.success)
    assert(result.offendingInstances.length == 0)
  }

  it should "return false on mutliple dc creator failing test owl file" in {
    val result = LinterTests.ontologyMustHaveOneDCCreator(creatorFailingOntology)
    assert(!result.success)
    assert(result.offendingInstances.length != 0)
  }

  "ontology-must-have-only-one-dc-contributor" should "return true on valid test owl file" in {
    val result = LinterTests.ontologyMustHaveOneDCContributor(passingOntology)
    assert(result.success)
    assert(result.offendingInstances.length == 0)
  }

  it should "return false on mutlple dc contributor failing test owl file" in {
    val result = LinterTests.ontologyMustHaveOneDCContributor(creatorFailingOntology)
    assert(!result.success)
    assert(result.offendingInstances.length != 0)
  }

  "ontology-must-have-dc-date" should "return true on valid test owl file" in {
    val result = LinterTests.ontologyMustHaveDCDate(passingOntology)
    assert(result.success)
    assert(result.offendingInstances.length == 0)
  }

  it should "return false on invalid test owl file" in {
    val result = LinterTests.ontologyMustHaveDCDate(failingOntology)
    assert(!result.success)
    assert(result.offendingInstances.length != 0)
  }

  "iris-and-labels-are-unique" should "return true on valid test owl file" in {
    val result = LinterTests.irisAndLabelsAreUnique(passingOntology)
    assert(result.success)
    assert(result.offendingInstances.length == 0)
  }

  it should "return false on invalid test owl file" in {
    val result = LinterTests.irisAndLabelsAreUnique(failingOntology)
    assert(!result.success)
    assert(result.offendingInstances.length != 0)
  }

  "non-root-classes-need-genus-differentiation" should "return true on valid test owl file" in {
    val result = LinterTests.nonRootClassesNeedGenusDifferentiation(genusDiffPassingOntology)
    assert(result.success)
    assert(result.offendingInstances.length == 0)
  }

  it should "return false on invalid test owl file" in {
    val result = LinterTests.nonRootClassesNeedGenusDifferentiation(failingOntology)
    assert(!result.success)
    assert(result.offendingInstances.length != 0)
  }
}

