#owLint [![Build Status](https://travis-ci.org/Samangan/owLint.svg?branch=master)](http://travis-ci.org/Samangan/owLint)


owLint is a configurable, command line OWL file linter. It utilizes the [OWL API](https://github.com/owlcs/owlapi).

Usage
=====

owLint will look in the defined directory for a .owlint configuration file and .owl files to lint.

An example .owlint file is seen below

```
{
  "ontology-must-have-version-info" : true,
  "ontology-must-have-dc-title" : true,
  "ontology-must-have-dc-creator" : true,
  "ontology-must-have-only-one-dc-creator" : true,
  "ontology-must-have-only-one-dc-contributor" : true,
  "ontology-must-have-dc-date" : true,
  "entities-must-have-rdfs-comment" : true,
  "iris-and-labels-are-unique" : true
}
```
A .owlint file is optional. By default all of the tests are enabled.


List of Options
=====================
##Ontology

###ontology-must-have-version-info
Each owl:Ontology must have an [owl:versionInfo](http://www.w3.org/TR/owl-ref/#versionInfo-def) annotation property.

###ontology-must-have-dc-title
###ontology-must-have-dc-creator
###ontology-must-have-only-one-dc-creator
Each dc:creator annotation can only have one name listed in the annotation. It is impossible to perfectly enforce this rule, but this can catch the most common ways people try to place more than one person into a single dc:creator annotation.

This test will fail if the dc:creator annotation contains any of the following: 
* new line, 
* carriage return, 
* tab, 
* vertical tab, 
* /, 
* _, 
* |,
* " and " (Notice the spaces padding the word. This is done to make sure that someone who has the substring and in their name will not make this lint fail). 

##Entities


###entities-must-have-rdfs-comment
All classes, individuals, object properties, data properties, and annotation properties defined within the currently linted IRI namespace must have rdfs:comment annotations.
  

Development
===========

###Compliling
sbt compile

###Building and Running
sbt "run -help"

###Testing
sbt test

###Adding additional linter options
To add your linter function add the function and full text description to the lintTestMappings Map.

The linter function you add must be in the following format:

```def superCoolLinterTest (ontology: OWLOntology): (Boolean, List[OffendingInstance]) = {/**/}```

If you add an additional option please add relevent tests.


Screenshots
===========
![screenshot](http://i.imgur.com/aKP7x86.png)

