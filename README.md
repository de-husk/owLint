owLint [![Build Status](https://travis-ci.org/Samangan/owLint.svg?branch=master)](http://travis-ci.org/Samangan/owLint)
------


owLint is a configurable, command line OWL file linter. It utilizes the [OWL API](https://github.com/owlcs/owlapi).

Usage
=====

owLint will look in the defined directory for a .owlint configuration file and .owl files to lint.

an example .owlint file is seen below

```
{
  "ontology-must-have-version-info" : true,
  "ontology-must-have-dc-title" : true,
  "ontology-must-have-dc-creator" : false,
  "ontology-must-have-only-one-dc-creator" : false,
  "entities-must-have-descriptions" : true
}
```
This will enable the "entities-must-have-descriptions" test. By default all of the tests are enabled.


List of Options
=====================
##Ontology

###ontology-must-have-version-info
###ontology-must-have-dc-title
###ontology-must-have-dc-creator
###ontology-must-have-only-one-dc-creator

##Entities

###entities-must-have-descriptions
All classes, individuals, object properties, data properties, and annotation properties defined within the currently linted IRI namespace but have rdf:descriptions annotations.
  

Development
===========

###Compliling
sbt compile

###Building and Running
sbt "run -help"

###Testing
sbt test

###Adding additional linter options
To add your linter function and the function and full text description to the lintTestMappings Map.

The linter function you add must be in the following format:

```def superCoolLinterTest (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {/**/}```

If you add an additional option please add relevent tests.


Screenshots
===========
![screenshot](http://i.imgur.com/aKP7x86.png)

