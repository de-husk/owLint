owLint
------


owLint is a configurable, command line OWL file linter.


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
This will enable the "entities-must-have-descriptions" parameter. By default all of the tests are enabled.


List of linter tests
=====================





Development
===========

To add more tests add to the lintTestMappings Map [String, Tuple2[Function1[OWLOntology, (Boolean, List[(String, String)])], String]] in OwLint.scala, and be sure to add the implementation of the linked Function1 you define. 

Every lint tester function has the following defintion:
  Input:
    * an OWLOntology object
  Output:
    * A Tuple containing whether or not the test was successful, and then a list of Tuples each containing the type of the offending thing and the string of the offending thing.
    
    TODO: Take the offending thing tuple and make it into a case class named: OffendingThing


