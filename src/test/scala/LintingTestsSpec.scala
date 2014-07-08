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

  val owLintRunner = new OwLint(owLintStarter.getOwLintConfig("fakepath"))

  "ontology-must-have-version-info" should "return true on proper test owl file" in {
    val result = owLintRunner.ontologyMustHaveVersionInfo(passingOntology)
    assert(result._1 == true)
    assert(result._2.length == 0)
  }

  "ontology-must-have-version-info" should "return false on failing test owl file" in {
    val result = owLintRunner.ontologyMustHaveVersionInfo(failingOntology)
    assert(result._1 == false)
    assert(result._2.length != 0)
  }
  //TODO: the rest of the lint tests

}
