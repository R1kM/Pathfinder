/**
Copyright (c) 2012-2014 Microsoft Corporation
   
Module Name:

    FixedpointDecRefQueue.java

Abstract:

Author:

    @author Christoph Wintersteiger (cwinter) 2012-03-15

Notes:
    
**/ 

package com.microsoft.z3;

class FixedpointDecRefQueue extends IDecRefQueue
{
    protected void incRef(Context ctx, long obj)
    {
        try
        {
            Native.fixedpointIncRef(ctx.nCtx(), obj);
        } catch (Z3Exception e)
        {
            // OK.
        }
    }

    protected void decRef(Context ctx, long obj)
    {
        try
        {
            Native.fixedpointDecRef(ctx.nCtx(), obj);
        } catch (Z3Exception e)
        {
            // OK.
        }
    }
};
