/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.List ;

import org.openjena.atlas.lib.RandomLib ;

import com.hp.hpl.jena.sparql.ARQNotImplemented ;

public class E_Random extends ExprFunctionN // 0 or one
{
    private static final String symbol = "rand" ;
    
    public E_Random() { this(null) ; }
    
    public E_Random(Expr expr)
    {
        super(symbol, expr) ;
    }
    
//    // Not really a special form but we need access to the env. 
//    @Override
//    public NodeValue evalSpecial(Binding binding, FunctionEnv env)
//    {
//    }
    
    @Override
    protected NodeValue eval(List<NodeValue> args)
    { 
        if ( args.size() == 1 )
        {
            throw new ARQNotImplemented("... yet") ;
//            int x = args.get(0).hashCode() ;
//            new Random()
        }

        double d = RandomLib.random.nextDouble() ;
        return NodeValue.makeDouble(d) ;
    }

    @Override
    protected Expr copy(ExprList newArgs)
    {
        if ( newArgs.size() == 0 )
            return new E_Random() ;
        else
            return new E_Random(newArgs.get(0)) ;
    } 
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */