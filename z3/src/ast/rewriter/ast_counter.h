/*++
Copyright (c) 2013 Microsoft Corporation

Module Name:

    ast_counter.h

Abstract:

    Routines for counting features of terms, such as free variables.

Author:

    Nikolaj Bjorner (nbjorner) 2013-03-18.
    Krystof Hoder (t-khoder) 2010-10-10.

Revision History:

    Hoisted from dl_util.h 2013-03-18.

--*/


#ifndef _AST_COUNTER_H_
#define _AST_COUNTER_H_

#include "ast.h"
#include "map.h"
#include "uint_set.h"

class counter {
protected:
    typedef u_map<int> map_impl;
    map_impl m_data;
    const bool m_stay_non_negative;
public:
    typedef map_impl::iterator iterator;
    
    counter(bool stay_non_negative = true) : m_stay_non_negative(stay_non_negative) {}
    
    void reset() { m_data.reset(); }
    iterator begin() const { return m_data.begin(); }
    iterator end() const { return m_data.end(); }    
    void update(unsigned el, int delta);
    int & get(unsigned el);

    /**
       \brief Increase values of elements in \c els by \c delta.
       
       The function returns a reference to \c *this to allow for expressions like
       counter().count(sz, arr).get_positive_count()
    */
    counter & count(unsigned sz, const unsigned * els, int delta = 1);
    counter & count(const unsigned_vector & els, int delta = 1) {
        return count(els.size(), els.c_ptr(), delta);
    }
    
    void collect_positive(uint_set & acc) const;
    unsigned get_positive_count() const;

    bool get_max_positive(unsigned & res) const;
    unsigned get_max_positive() const;

    /**
       Since the default counter value of a counter is zero, the result is never negative.
    */
    int get_max_counter_value() const;
};

class var_counter : public counter {
protected:
    ptr_vector<sort> m_sorts;
    expr_fast_mark1  m_visited;
    ptr_vector<expr> m_todo;
    ast_mark         m_mark;
    unsigned_vector  m_scopes;
    unsigned get_max_var(bool & has_var);    
public:
    var_counter(bool stay_non_negative = true): counter(stay_non_negative) {}
    void count_vars(ast_manager & m, const app * t, int coef = 1);
    unsigned get_max_var(expr* e);
    unsigned get_next_var(expr* e);
};

class ast_counter {
    typedef obj_map<ast, int> map_impl;
    map_impl m_data;
    bool     m_stay_non_negative;
 public:
    typedef map_impl::iterator iterator;
    
    ast_counter(bool stay_non_negative = true) : m_stay_non_negative(stay_non_negative) {}
    
    iterator begin() const { return m_data.begin(); }
    iterator end() const { return m_data.end(); }
    
    int & get(ast * el) {
        return m_data.insert_if_not_there2(el, 0)->get_data().m_value;
    }
    void update(ast * el, int delta){
        get(el) += delta;
        SASSERT(!m_stay_non_negative || get(el) >= 0);
    }
    
    void inc(ast * el) { update(el, 1); }
    void dec(ast * el) { update(el, -1); }
};

#endif
