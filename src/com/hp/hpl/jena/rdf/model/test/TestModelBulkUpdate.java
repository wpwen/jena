/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestModelBulkUpdate.java,v 1.1 2003-04-23 11:00:15 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import junit.framework.*;

/**
    Tests of the Model-level bulk update API.
    
 	@author kers
*/

public class TestModelBulkUpdate extends ModelTestBase
    {
    public TestModelBulkUpdate( String name )
        { super( name ); };
        
    public static TestSuite suite()
        { return new TestSuite( TestModelBulkUpdate.class ); }   

    public void testMBU()
        { testMBU( ModelFactory.createDefaultModel() ); }
        
    public void testContains( Model m, Statement [] statements )
        {
        for (int i = 0; i < statements.length; i += 1)
            assertTrue( "it should be here", m.contains( statements[i] ) );
        }
    
    public void testContains( Model m, List statements )
        {
        for (int i = 0; i < statements.size(); i += 1)
            assertTrue( "it should be here", m.contains( (Statement) statements.get(i) ) );
        }
        
    public void testOmits( Model m, Statement [] statements )
        {
        for (int i = 0; i < statements.length; i += 1)
            assertFalse( "it should not be here", m.contains( statements[i] ) );
        }

    public void testOmits( Model m, List statements )
        {
        for (int i = 0; i < statements.size(); i += 1)
            assertFalse( "it should not be here", m.contains( (Statement) statements.get(i) ) );
        }
                
    public void testMBU( Model m )
        {
        Statement [] sArray = statements( m, "moon orbits earth; earth orbits sun" );
        List sList = Arrays.asList( statements( m, "I drink tea; you drink coffee" ) );
        m.add( sArray );
        testContains( m, sArray );
        m.add( sList );
        testContains( m, sList );
        testContains( m, sArray );
    /* */
        m.remove( sArray );
        testOmits( m, sArray );
        testContains( m, sList );    
        m.remove( sList );
        testOmits( m, sArray );
        testOmits( m, sList );
        }
        
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/