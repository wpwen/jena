/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashedTripleBunch.java,v 1.12 2006-04-28 08:23:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

public class HashedTripleBunch extends HashCommon implements TripleBunch
    {
    protected int changes;
    
    public HashedTripleBunch( TripleBunch b )
        {
        super( nextSize( (int) (b.size() / loadFactor) ) );
        for (Iterator it = b.iterator(); it.hasNext();) add( (Triple) it.next() );        
        changes = 0;
        }

    public boolean contains( Triple t )
        { return findSlot( t ) < 0; }    
    
    protected int findSlotBySameValueAs( Triple key )
        {
        int index = initialIndexFor( key );
        // int k = 1;
        while (true)
            {
            Object current = keys[index];
            if (current == null) return index;
            if (key.matches( (Triple) current )) return ~index;
            if (--index < 0) index += capacity;
            }
        }
    
    public boolean containsBySameValueAs( Triple t )
        { return findSlotBySameValueAs( t ) < 0; }
    
    /**
        Answer the number of items currently in this TripleBunch. 
        @see com.hp.hpl.jena.mem.TripleBunch#size()
    */
    public int size()
        { return size; }
    
    /**
        Answer the current capacity of this HashedTripleBunch; for testing purposes
        only. [Note that the bunch is resized when it is more than half-occupied.] 
    */
    public int currentCapacity()
        { return capacity; }
    
    public void add( Triple t )
        {
        keys[findSlot( t )] = t;
        changes += 1;
        if (++size > threshold) grow();
        }
    
    protected void grow()
        {
        Object [] oldContents = keys;
        final int oldCapacity = capacity;
        growCapacityAndThreshold();
        Object [] newKeys = keys = new Triple[capacity];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Object t = oldContents[i];
            if (t != null) newKeys[findSlot( t )] = t;
            }
        }
    
    public void remove( Triple t )
        {
        removeFrom( ~findSlot( t ) );
        size -= 1;
        changes += 1;
        }
    
    public ExtendedIterator iterator()
        {
        return new NiceIterator()
            {
            int index = capacity;
            int lastIndex = -1;
            final int initialChanges = changes;
            Object toRemove = null;
            Object current = null;
                        
            public boolean hasNext()
                {
                if (changes > initialChanges) throw new ConcurrentModificationException();
                if (current == null)
                    while (index > 0 && ((current = keys[--index]) == null)) {};
                return current != null;
                }
            
            public Object next()
                {
                if (changes > initialChanges) throw new ConcurrentModificationException();
                if (current == null && hasNext() == false) noElements( "HashedTripleBunch iterator empty" );
                Object answer = toRemove = current;
                lastIndex = index;
                current = null;
                return answer;
                }
            
            /**
                Removing an element resets the iterator to continue from the removed
                position, in case an element has been shuffled up into the vacated
                position. 
            */
            public void remove()
                {
                if (keys[lastIndex] != toRemove) throw new ConcurrentModificationException();
                HashedTripleBunch.this.removeFrom( lastIndex );
                current = keys[index = lastIndex];
                lastIndex = -1;
                }
            };
        }
    
    public void app( Domain d, StageElement next, MatchOrBind s )
        {
        int i = capacity;
        while (i > 0)
            {
            Object t = keys[--i];
            if (t != null  && s.matches( (Triple) t )) next.run( d );
            }
        }
    }

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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