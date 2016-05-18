# Symbolic Pathfinder

## Abstract

Symbolic Pathfinder (SPF) is a symbolic execution tool, based on NASA [Java Pathfinder](babelfish.arc.nasa.gov/trac/jpf) (JPF) model checker. It executes Java bytecode using a custom JVM to perform its analysis.
Our goals are the following :
*   Add support for symbolic arrays : achieved.
*   Implement a replay module for the execution of the scenarii with concrete examples
*   Use [jConstraints](https://github.com/psycopaths/jconstraints) as a solver abstraction layer

## How to Use it

TODO

## Symbolic Execution of arrays

Support for arrays is currently enabled using the Z3 solver.
For the moment, we support all arrays but float arrays, since 
our version of Z3 does not have a support for floats.

See Arrays examples in the src/examples folder.

If Z3 can't be found, make sure that LD_LIBRARY_PATH contains the Pathfinder/jpf-symbc/lib directory.

### Overview

A symbolic array is composed of a unique name, a symbolic integer representing its length, 
and constraints about which objects/values are in it.

During the execution of LOAD and STORE instruction, we add constraints linking our current object to
the symbolic array. 
The following constraints are added :
* The index (that can be symbolic) is greater than 0
* The index (that can be symbolic) is smaller than the array length
* The object we retrieve/store is at the current index
Z3 array theory uses *select* and *store* constraints.
*select* takes an array *a*, and an index *i*, and returns an object *v*. 
Given an object *v2*, we can add the constraint a[i] ( = v) = v2 or a[i] != v2
*store* takes an array *a*, an index *i* and an object *v* and returns a new array *a2*.
*a2* is equal to *a*, with the exception of the object/value stored at index *i* that is equal to *v*.
It is strong enough to solve our constraints, and generate a model is it is satisfiable.

### Lazy initialization

When an array contains objects, and not primitive values, we use lazy initialization to model them.
When we load an object, we have three possibilities:
* The object is equal to null
* The object is equal to a previously initialized object
* The object is a new object
Each object is referenced by a unique reference *objRef*. Thus, to link objects to the symbolic array, 
we simply add constraints using *select/store* in Z3, and *objRef*.




