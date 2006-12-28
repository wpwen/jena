/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import static com.hp.hpl.jena.query.engine2.AlgebraCompilerQuad.defaultGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine2.op.Quad;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.compiler.QC;
import com.hp.hpl.jena.sdb.core.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.core.compiler.QuadBlockCompilerTriple;
import com.hp.hpl.jena.sdb.core.compiler.SDBConstraint;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;

public abstract class QuadBlockCompiler2 extends QuadBlockCompilerTriple
{
    // Slot typing.
    // Result handling.
    
    private static Log log = LogFactory.getLog(QuadBlockCompiler2.class) ;
    
    private Generator genNodeResultAlias = Gensym.create(Aliases.NodesResultAliasBase) ;

    List<Node> constants = new ArrayList<Node>() ;
    List<Var>  vars = new ArrayList<Var>() ;
    
    public QuadBlockCompiler2(SDBRequest request)
    { super(request) ; }

    @Override
    final protected SqlNode start(QuadBlock quads)
    {
        classify(quads, constants, vars) ;
        addMoreConstants(constants) ;
        SqlNode sqlNode = insertConstantAccesses(request, constants) ;
        return sqlNode ;
    }

    // Hook for specialized engines.
    protected void addMoreConstants(Collection<Node> constants)
    {}

    @Override
    final protected SqlNode finish(SqlNode sqlNode, QuadBlock quads)
    {
        //sqlNode = addRestrictions(request, sqlNode, getConstraints()) ;
        
        // TODO Work out the value tests and project vars needed.
        // Currently, every block setting a variable will get its result.
        // Intersection of variables in this quad block and the output needed.
        
        List<Var> projectVars = vars ;
        sqlNode = extractResults(request, projectVars , sqlNode) ;
        return sqlNode ;

    }
    
    protected abstract
    SqlColumn getNodeMatchCol(SqlTable nodeTable) ;
    
    protected abstract 
    SqlNode insertConstantAccesses(SDBRequest request, Collection<Node> constants) ;

    private SqlNode extractResults(SDBRequest request,
                                   Collection<Var>vars, SqlNode sqlNode)
    {
        // for each var and it's id column, make sure there is value column. 
        for ( Var v : vars )
        {
            SqlColumn c1 = sqlNode.getIdScope().getColumnForVar(v) ;
            if ( c1 == null )
            {
                // Debug.
                Scope scope = sqlNode.getIdScope() ;
                // Variable not actually in results.
                continue ;
            }

            // Already in scope from a condition?
            SqlColumn c2 = sqlNode.getValueScope().getColumnForVar(v) ;
            if ( c2 != null )
                // Already there
                continue ;

            // Not in scope -- add a table to get it (share some code with addRestrictions?) 
            // Value table.
            SqlTable nTable = new TableNodes(genNodeResultAlias.next()) ;
            c2 = getNodeMatchCol(nTable) ; // "id" or "hash"

            nTable.setValueColumnForVar(v, c2) ;
            // Condition for value: triple table column = node table id 
            nTable.addNote("Var: "+v) ;


            SqlExpr cond = new S_Equal(c1, c2) ;
            SqlNode n = QC.innerJoin(request, sqlNode, nTable) ;
            sqlNode = SqlRestrict.restrict(n, cond) ;
        }
        return sqlNode ;
    }

    private SqlNode addRestrictions(SDBRequest request,
                                    SqlNode sqlNode,
                                    List<SDBConstraint> constraints)
    {
        if ( constraints.size() == 0 )
            return sqlNode ;

        // Add all value columns
        for ( SDBConstraint c : constraints )
        {
            @SuppressWarnings("unchecked")
            Set<Var> vars = c.getExpr().getVarsMentioned() ;
            for ( Var v : vars )
            {
                // For Variables used in this SQL constraint, make sure the value is available.  

                SqlColumn tripleTableCol = sqlNode.getIdScope().getColumnForVar(v) ;   // tripleTableCol
                if ( tripleTableCol == null )
                {
                    // Not in scope.
                    log.info("Var not in scope for value of expression: "+v) ;
                    continue ;
                }

                // Value table column
                SqlTable nTable =   new TableNodes(genNodeResultAlias.next()) ;
                SqlColumn colId =   new SqlColumn(nTable, "id") ;
                SqlColumn colLex =  new SqlColumn(nTable, "lex") ;
                SqlColumn colType = new SqlColumn(nTable, "type") ;

                nTable.setValueColumnForVar(v, colLex) ;        // ASSUME lexical/string form needed 
                nTable.setIdColumnForVar(v, colId) ;            // Id scope => join
                sqlNode = QC.innerJoin(request, sqlNode, nTable) ;
            }

            SqlExpr sqlExpr = c.compile(sqlNode.getValueScope()) ;
            sqlNode = SqlRestrict.restrict(sqlNode, sqlExpr) ;
        }
        return sqlNode ;
    }

    @Override
    final protected SqlTable accessTriplesTable(String alias)
    {
        return new TableTriples(alias) ;
    }
    
    private static void classify(QuadBlock quadBlock, Collection<Node> constants, Collection<Var>vars)
    {
        for ( Quad quad : quadBlock )
        {
            if ( ! quad.getGraph().equals(defaultGraph) )
            {
                log.fatal("Non-default graph") ;
                throw new SDBException("Non-default graph") ;
            }
            if ( false )
                // Not quadding currently.
                acc(constants, vars, quad.getGraph()) ;
            acc(constants, vars, quad.getSubject()) ;
            acc(constants, vars, quad.getPredicate()) ;
            acc(constants, vars, quad.getObject()) ;
        }
    }

    private static void acc(Collection<Node>constants,  Collection<Var>vars, Node node)
    { 
        // ?? node.isConcrete()
        if ( node.isLiteral() || node.isBlank() || node.isURI() )
        {
            constants.add(node) ;
            return ;
        }
        if ( Var.isVar(node) )
        {
            vars.add(Var.alloc(node)) ;
            return ;
        }
        if ( node.isVariable() )
        {
            log.warn("Node_Varable but not a Var; bodged") ;
            vars.add(Var.alloc(node)) ;
            return ;
        }
        log.fatal("Unknown Node type: "+node) ;
        throw new SDBException("Unknown Node type: "+node) ;
    }
    
    //private static 
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
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