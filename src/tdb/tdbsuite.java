/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.PrintStream;

import junit.framework.TestSuite;

import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.tdb.InstallationTest;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.junit.TestFactoryTDB;

/** Run test script suites */
public class tdbsuite
{
    public static void main(String ... argv)
    {
        if ( argv.length == 0 )
        {
            System.err.println(Utils.classShortName(tdbsuite.class)+": No manifest file (did you mean to run tdbverify?)") ;
            System.exit(1) ;
            
        }
        
        if ( argv.length != 1 )
        {
            System.err.println(Utils.classShortName(tdbsuite.class)+"Required: test manaifest file") ;
            System.exit(1) ;
        }
        
        String manifestFile = argv[0] ;
        
        PrintStream out = System.out ;
        if ( TDB.VERSION.equals("DEV") )
            out.printf("TDB (development) %s\n", manifestFile) ;
        else
            out.printf("TDB v%s (Built: %s) %s\n", TDB.VERSION, TDB.BUILD_DATE, manifestFile) ;

        TestSuite ts = new TestSuite() ;
        TestFactoryTDB.make(ts, manifestFile, "TDB-", TDBFactory.stdFactory) ;
        
        JUnitCore runner = new org.junit.runner.JUnitCore() ;
        runner.addListener(new MyTextListener(out)) ;
        
        InstallationTest.beforeClass() ;
        Result result = runner.run(ts) ;
        InstallationTest.afterClass() ;
        
        if ( result.getFailureCount() > 0 )
            System.exit(1) ;
        
    }

    // Factor out - share with tdbtest
    static class MyTextListener extends TextListener
    {

        private PrintStream out ;
        int count = 0 ;

        public MyTextListener(PrintStream writer)
        {
            super(writer) ;
            this.out = writer ;
        }
        
        @Override
        public void testRunStarted(Description description)
        {
            //count = 0 ;
        }
        
        @Override
        public void testStarted(Description description) {
            newline() ;
            out.append('.');
        }

        private void newline()
        {
            if ( count != 0 && count%50 == 0 )
                out.println();
            count++ ;
        }

        @Override
        public void testFailure(Failure failure) {
            newline() ;
            out.append('E');
        }

        @Override
        public void testIgnored(Description description) {
            newline() ;
            out.append('I');
        }
        
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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