/**
Copyright (c) 2012-2014 Microsoft Corporation
   
Module Name:

    FiniteDomainSort.java

Abstract:

Author:

    @author Christoph Wintersteiger (cwinter) 2012-03-15

Notes:
    
**/

package com.microsoft.z3;

/**
 * Finite domain sorts.
 **/
public class FiniteDomainSort extends Sort
{
	/**
	 * The size of the finite domain sort.
	 **/
	public long getSize() throws Z3Exception
	{
		Native.LongPtr res = new Native.LongPtr();
		Native.getFiniteDomainSortSize(getContext().nCtx(), getNativeObject(), res);
		return res.value;
	}

	FiniteDomainSort(Context ctx, long obj) throws Z3Exception
	{
		super(ctx, obj);
	}

	FiniteDomainSort(Context ctx, Symbol name, long size) throws Z3Exception
	{
		super(ctx, Native.mkFiniteDomainSort(ctx.nCtx(), name.getNativeObject(),
				size));
	}
}
