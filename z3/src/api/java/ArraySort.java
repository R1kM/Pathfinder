/**
Copyright (c) 2012-2014 Microsoft Corporation
   
Module Name:

    ArraySort.java

Abstract:

Author:

    @author Christoph Wintersteiger (cwinter) 2012-03-15

Notes:
    
**/

package com.microsoft.z3;

/**
 * Array sorts.
 **/
public class ArraySort extends Sort
{
	/**
	 * The domain of the array sort.
	 * @throws Z3Exception 
	 **/
	public Sort getDomain() throws Z3Exception
	{
		return Sort.create(getContext(),
				Native.getArraySortDomain(getContext().nCtx(), getNativeObject()));
	}

	/**
	 * The range of the array sort.
	 * @throws Z3Exception 
	 **/
	public Sort getRange() throws Z3Exception
	{
		return Sort.create(getContext(),
				Native.getArraySortRange(getContext().nCtx(), getNativeObject()));
	}

	ArraySort(Context ctx, long obj) throws Z3Exception
	{
		super(ctx, obj);
	}

	ArraySort(Context ctx, Sort domain, Sort range) throws Z3Exception
	{
		super(ctx, Native.mkArraySort(ctx.nCtx(), domain.getNativeObject(),
				range.getNativeObject()));
	}
};
