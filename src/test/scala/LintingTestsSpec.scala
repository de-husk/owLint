import org.scalatest.{FlatSpec, Matchers}
import java.io.File
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology

import owLint.OwLint

class LintingTestsSpec extends FlatSpec with Matchers{
  val ontologyManager: OWLOntologyManager = OWLManager.createOWLOntologyManager
  
  val passingOntology: OWLOntology = ontologyManager.loadOntologyFromOntologyDocument(new File("./test/pizza.owl"))
  val failingOntology: OWLOntology = ontologyManager.loadOntologyFromOntologyDocument(new File("./test/pizza-fails.owl"))
  val creatorFailingOntology: OWLOntology = ontologyManager.loadOntologyFromOntologyDocument(new File("./test/pizza-more-than-one-creator-fails.owl"))
  val owLintRunner = new OwLint(OwLintStarter.getOwLintConfig("fakepath"))

  "ontology-must-have-version-info" should "return true on proper test owl file" in {
    val result = owLintRunner.ontologyMustHaveVersionInfo(passingOntology)
    assert(result._1 == true)
    assert(result._2.length == 0)
  }

  it should "return false on failing test owl file" in {
    val result = owLintRunner.ontologyMustHaveVersionInfo(failingOntology)
    assert(result._1 == false)
    assert(result._2.length != 0)
  }
 
  "ontology-must-have-dc-title" should "return true on proper test owl file" in {
    val result = owLintRunner.ontologyMustHaveDCTitle(passingOntology)
    assert(result._1 == true)
    assert(result._2.length == 0)
  }

  it should "return false on failing test owl file" in {
    val result = owLintRunner.ontologyMustHaveDCTitle(failingOntology)
    assert(result._1 == false)
    assert(result._2.length != 0)
  }

  "ontology-must-have-only-one-dc-creator" should "return true on proper test owl file" in {
    val result = owLintRunner.ontologyMustHaveOneDCCreator(passingOntology)
    assert(result._1 == true)
    assert(result._2.length == 0)
  }

  it should "return false on mutliple dc creator failing test owl file" in {
    val result = owLintRunner.ontologyMustHaveOneDCCreator(creatorFailingOntology)
    assert(result._1 == false)
    assert(result._2.length != 0)
  }

  "ontology-must-have-only-one-dc-contributor" should "return true on valid test owl file" in {
    val result = owLintRunner.ontologyMustHaveOneDCContributor(passingOntology)
    assert(result._1 == true)
    assert(result._2.length == 0)
  }

  it should "return false on mutlple dc contributor failing test owl file" in {
    val result = owLintRunner.ontologyMustHaveOneDCContributor(creatorFailingOntology)
    assert(result._1 == false)
    assert(result._2.length != 0)
  }

  "ontology-must-have-dc-date" should "return true on valid test owl file" in {
    val result = owLintRunner.ontologyMustHaveDCDate(passingOntology)
    assert(result._1 == true)
    assert(result._2.length == 0)
  }

  it should "return false on invalid test owl file" in {
    val result = owLintRunner.ontologyMustHaveDCDate(failingOntology)
    assert(result._1 == false)
    assert(result._2.length != 0)
  }
}
