/*
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

package com.hp.hpl.jena.n3.turtle;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;

/** Map from _:* form to bNodes
 * 
 * @author Andy Seaborne
 * @version $Id: LabelToNodeMap.java,v 1.1 2009-06-29 08:55:45 castagna Exp $
 */

public class LabelToNodeMap
{
    Map<String, Node> bNodeLabels = new HashMap<String, Node>() ;
    
    public LabelToNodeMap()
    {}

    public Node asNode(String label)
    {
        Node n = bNodeLabels.get(label) ;
        if ( n != null )
            return n ;
        n = allocNode() ;
        bNodeLabels.put(label, n) ;
        return n ;
    }
    
    public Node allocNode()
    {
        return Node.createAnon() ;
    }
    
    public void clear()
    {
        bNodeLabels.clear() ;
    }
}
