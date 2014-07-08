owLint
------


owLint is a configurable, command line OWL file linter. It utilizes the [OWL API](https://github.com/owlcs/owlapi).

Usage
=====

owLint will look in the defined directory for a .owlint configuration file and .owl files to lint.

an example .owlint file is seen below

```
{
  "entities-must-have-descriptions" : true
  ...
}

```
This will enable the "entities-must-have-descriptions" test. By default all of the tests are enabled.


List of linter tests
=====================
  * ontology-must-have-version-info
  * ontology-must-have-dc-title
  * ontology-must-have-dc-creator
  * ontology-must-have-only-one-dc-creator
  

Development
===========

To add more tests add to the lintTestMappings Map [String, Tuple2[Function1[OWLOntology, (Boolean, List[(String, String)])], String]] in OwLint.scala, and be sure to add the implementation of the linked Function1 you define. See the existing mappings for examples

Every lint tester function has the following defintion:
###Input:
 * an OWLOntology object

###Output:
 * A Tuple containing whether or not the test was successful, and an OffendingInstance case class object.
   

Screenshots
===========
![screenshot](http://i.imgur.com/aKP7x86.png)

