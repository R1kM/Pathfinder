/**
Copyright (c) 2012-2014 Microsoft Corporation
   
Module Name:

    RealExpr.java

Abstract:

Author:

    @author Christoph Wintersteiger (cwinter) 2012-03-15

Notes:
    
**/ 

package com.microsoft.z3;

/**
 * Real expressions
 **/
public class RealExpr extends ArithExpr
{
    /**
     * Constructor for RealExpr </summary>
     **/
    protected RealExpr(Context ctx)
    {
        super(ctx);
    }

    RealExpr(Context ctx, long obj) throws Z3Exception
    {
        super(ctx, obj);
    }
}
