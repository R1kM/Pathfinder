/**
Copyright (c) 2012-2014 Microsoft Corporation
   
Module Name:

    BitVecExpr.java

Abstract:

Author:

    @author Christoph Wintersteiger (cwinter) 2012-03-15

Notes:
    
**/

package com.microsoft.z3;

/**
 * Bit-vector expressions
 **/
public class BitVecExpr extends Expr
{

	/**
	 * The size of the sort of a bit-vector term.
	 * @throws Z3Exception 
	 **/
	public int getSortSize() throws Z3Exception
	{
		return ((BitVecSort) getSort()).getSize();
	}

	/**
	 * Constructor for BitVecExpr </summary>
	 **/
	BitVecExpr(Context ctx)
	{
		super(ctx);
	}

	BitVecExpr(Context ctx, long obj) throws Z3Exception
	{
		super(ctx, obj);
	}
}
