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


List of Options
=====================
  * ontology-must-have-version-info
  * ontology-must-have-dc-title
  * ontology-must-have-dc-creator
  * ontology-must-have-only-one-dc-creator
  * entities-must-have-descriptions
  

Development
===========

###Compliling
sbt compile

###Building and Running
sbt "run -help"

###Testing
sbt test

###Adding additional linter tests
To add your linter function and the function and full text description to the lintTestMappings Map.

The linter function you add must be in the following format:

```def superCoolLinterTest (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {/**/}```


Screenshots
===========
![screenshot](http://i.imgur.com/aKP7x86.png)

