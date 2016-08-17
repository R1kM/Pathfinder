# Symbolic Pathfinder

## Abstract

Symbolic Pathfinder (SPF) is a symbolic execution tool, based on NASA [Java Pathfinder](babelfish.arc.nasa.gov/trac/jpf) (JPF) model checker. It executes Java bytecode using a custom JVM to perform its analysis.
Our goals are the following :
*   Add support for symbolic arrays : achieved.
*   Implement a replay module for the execution of the scenarii with concrete examples : In progress

This [paper](https://github.com/R1kM/Pathfinder/blob/master/SymbolicArrays.pdf) submitted to the JPF Workshop sums up the improvements to SPF.

Most of this work was done on a private repository on Bitbucket. The list of commits from the beginning of Google Summer of Code can be found [here](https://github.com/R1kM/Pathfinder/blob/master/hgcommits)

## Symbolic Execution of arrays

Support for arrays is enabled using the Z3 solver.
All arrays are supported.
This support is optional : To enable it, specify `symbolic.arrays=true` in the .jpf configuration file

See Arrays examples in the src/examples/arrays folder.

A Dockerfile is provided to use SPF. Just run
`docker build -t spf .`
`docker run -it spf`

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


## Installing and configuring JavaPathFinder

### .properties files

#### site.properties

In your home, create a .jpf directory. Then, create a site.properties inside the .jpf dir, looking like that:
```
jpf.home = /path/to/JavaPathFinder

jpf-core = /path/to/JavaPathFinder/jpf-core

jpf-symbc = /path/to/JavaPathFinder/jpf-symbc
extensions = ${jpf-core},${jpf-symbc}
```
Please note that `${user.home}` can be used as a replacement for /path/to/home.

#### jpf.properties

These files are already configured. A jpf.properties file is required in each module.
To configure the classpath, change the `jpf-symbc.classpath` variable in the jpf-symbc/jpf.properties file.

If more information is needed, please see the official [wiki](babelfish.arc.nasa.gov/trac/jpf/wiki/user/run).

### Build PathFinder

Just run `ant build` in the jpf-core *and* in the jpf-symbc directories.

## Running PathFinder

Just execute `jpf` located in the jpf-core/bin/ directory on a .jpf file.
Some examples are in the `src/examples/` directory. For instance, from the JavaPathFinder directory:
```
./jpf-core/bin/jpf jpf-symbc/src/examples/simple/Branches.jpf
```
When using z3, if an NoClassDefFoundError is raised, make sure that LD_LIBRARY_PATH is set and points to jpf-symbc/lib. 

# The annotation system

JPF runs on `.class` files using annotations in a .jpf file.
Here is a basic .jpf file:
```
target = ClassTest

symbolic.method = ClassTest.test(sym)
symbolic.lazy = on
listener = gov.nasa.jpf.symbc.heap.HeapSymbolicListener
search.multiple_errors = true
```

The target argument tells which classfile JPF has to look for.

The symbolic.method argument tells which methods have to be symbolically tested. The arguments can be either symbolic or concrete.
For instance, for a method having two arguments, where only the first has to be symbolically evaluated, the syntax would be
symbolic.method = method(sym#con).
Please note that the number of arguments in the .jpf file has to match the actual number of arguments of the method.

Listeners are used for searching and checking different properties at runtime. The HeapSymbolicListener is currently the one that should be used.

The symbolic.lazy argument tells to use lazy initialization.

The search.multiple_errors argument tells SPF not to stop at the first error.

It is possible to change the search heuristic of SPF by specifying search.class = .search.heuristic.BFSHeuristic for instance (default is DFS search).
Further information regarding search heuristics will be included in the wiki.

For further information regarding annotations, please check the non-exhaustive list in the official [wiki](babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-symbc/doc).


