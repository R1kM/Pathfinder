# Pathfinder

Extension of [Java Pathfinder](babelfish.arc.nasa.gov/trac/jpf), to support symbolic execution of arrays.

See Arrays examples in the src/examples folder.

Support for arrays is enabled with Z3.  
If Z3 can't be found, make sure that LD_LIBRARY_PATH contains the Pathfinder/jpf-symbc/lib directory.

Symbolic execution of primitive types arrays is under test.  
Symbolic execution of objects is planned.



