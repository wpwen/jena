/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.engine.binding;

import static org.openjena.riot.tokens.TokenType.DOT ;
import static org.openjena.riot.tokens.TokenType.IRI ;
import static org.openjena.riot.tokens.TokenType.PREFIXED_NAME ;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.IteratorSlotted ;
import org.openjena.riot.Lang ;
import org.openjena.riot.lang.LangEngine ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.system.RiotLib ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.TokenType ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.core.Var ;

/** Language for reading in a steram of bindings.
 * See <a href="https://cwiki.apache.org/confluence/display/JENA/BindingIO">BindingIO</a>
 * 
 * <p>Summary:</p>
 * <ul>
 * <li>Directives:
 *   <ul>
 *     <li>VARS - list of variables.</li>
 *     <li>PREFIX</li>
 *   </ul>
 *  </li> 
 * <li>Lines of RDF terms (Turtle, no triple-quoted strings)</li>
 * <li>Items on line align with last VARS declaration</li>
 * <li>* for "same as last row"</li>
 * <li>- for "undef"</li>
 * </ul>
 */
public class BindingInputStream extends LangEngine implements Iterator<Binding>
{
    // In effect, multiple Inheritance.
    // We implementation-inherit from LangEngine(no public methods) 
    // and also IteratorTuples (redirecting calls to be object) 
    private final IteratorTuples iter ;
    
    public BindingInputStream(InputStream in)
    {
        this(TokenizerFactory.makeTokenizerUTF8(in)) ;
    }
    
    public BindingInputStream(Tokenizer tokenizer)
    {
        this(tokenizer,  RiotLib.profile(Lang.TURTLE, null)) ;
    }
    
    /** Create an RDF Tuples parser.
     *  No need to pass in a buffered InputStream; the code 
     *  will do it's own buffering.
     */
    
    private BindingInputStream(Tokenizer tokenizer, ParserProfile profile)
    {
        super(tokenizer, profile) ;
        iter = new IteratorTuples() ;
        
        // Fixes to TokenizerText
        //  peekToken
        //  CNTRL_CHAR no letter -> CH_STAR
        //  CNTRL off and type SYMBOL for >1 chars 
        // TODO
        // Resturcture to make lookingAt,nextToken and peekToken statics.
        
        //TokenizerText.CTRL_CHAR = Chars.B_SEMICOLON ;
        
    }

    //@Override
    public boolean hasNext()
    {
        return iter.hasNext() ;
    }

    //@Override
    public Binding next()
    {
        return iter.next() ;
    }

    //@Override
    public void remove()
    { iter.remove() ; }

    class IteratorTuples extends IteratorSlotted<Binding>
    {
        private Binding lastLine ;
        private List<Var> vars = new ArrayList<Var>() ;

        @Override
        protected Binding moveToNext()
        {
            while ( lookingAt(TokenType.KEYWORD) )
            {
                Token t = nextToken() ;
                if ( t.getImage().equalsIgnoreCase("VARS") )
                {
                    directiveVars() ;
                    continue ;
                }
                if ( t.getImage().equalsIgnoreCase("PREFIX") )
                {
                    directivePrefix() ;
                    continue ;
                }
            }
            Binding binding = new BindingMap(null) ;

            int i = 0 ;
            
            while( ! lookingAt(TokenType.DOT) )
            {
                if ( i >= vars.size() )
                    exception(peekToken(), "Too many items in a line.  Expected "+vars.size()) ;
                
                Var v = vars.get(i) ;
                
                Token token = nextToken() ;
                if ( ! token.hasType(TokenType.MINUS ) )
                {
                    Node n ;
                    // One case; VARS line then *
                    // HACK
                    if ( token.hasType(TokenType.STAR ) || ( token.isCtlCode() && token.getCntrlCode() == -1 ) )
                        n = lastLine.get(v) ;
                    else
                        n = profile.create(null, token) ;
                    binding.add(v, n) ;
                }
                i++ ;
            }
            if ( eof() )
                exception(peekToken(), "Line does not end with a DOT") ;
            
            Token dot = nextToken() ;
            
            if ( i != vars.size() )
            {
                Var v = vars.get(vars.size()-1) ;
                exception(dot, "Too many items in a line.  Expected "+vars.size()) ;
            }
            lastLine = binding ;
            return binding ;
        }

        @Override
        protected boolean hasMore()
        {
            return moreTokens() ;
        }
     
        private void directiveVars()
        {
            vars.clear() ;
            while (! eof() && ! lookingAt(DOT) )
            {
                Token t = nextToken() ;
                if ( ! t.hasType(TokenType.VAR) )
                    exception(t, "VARS requires a list of variables (found '"+t+"')") ;
                Var v = Var.alloc(t.getImage()) ;
                vars.add(v) ;
            }
            nextToken() ;   // DOT
        }

        private void directivePrefix()
        {
            if ( ! lookingAt(PREFIXED_NAME) )
                exception(peekToken(), "PREFIX requires a prefix (found '"+peekToken()+"')") ;
            if ( peekToken().getImage2().length() != 0 )
                exception(peekToken(), "PREFIX requires a prefix and no suffix (found '"+peekToken()+"')") ;
            String prefix = peekToken().getImage() ;
            nextToken() ;
            if ( ! lookingAt(IRI) )
                exception(peekToken(), "@prefix requires an IRI (found '"+peekToken()+"')") ;
            String iriStr = peekToken().getImage() ;
            IRI iri = profile.makeIRI(iriStr, currLine, currCol) ;
            profile.getPrologue().getPrefixMap().add(prefix, iri) ;
            nextToken() ;
            expect("PREFIX directive not terminated by a dot", DOT) ;
        }
    }
}
