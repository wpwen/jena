package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 * @author jjc
 *
 */
abstract class CNode extends EnhNode implements CNodeI {
	final static public Implementation factory = new Implementation() {
		public EnhNode wrap(Node n, EnhGraph eg) {
				// work out what sort of node this is.
	if (n.isLiteral())
				return new CLit(n, eg);
			if (n.isURI()) {
				int type = Grammar.getBuiltinID(n.getURI());
				if (type != -1)
					return new CBuiltin(n, eg, type);
				return new CURIref(n, eg);
			}
			return new CBlank(n, eg);
		}
	};
	CNode(Node n, EnhGraph eg) {
		super(n, eg);
	}
	public void incrObjectCount(int i) {
	}

	Node getAttribute(Node property) {
		Graph g = getGraph().asGraph();
		ClosableIterator it = g.find(asNode(), property, null);
		Node rslt = null;
		try {
			if (it.hasNext()) {
				rslt = ((Triple) it.next()).getObject();
				if (it.hasNext()) {
					throw new SyntaxException(
						"Internal error: <"
							+ property.getURI()
							+ "> may have at most one value.");
				}
			}
		} finally {
			it.close();
		}
		return rslt;
	}
	void setAttribute(Node property, Node obj) {
		Graph g = getGraph().asGraph();
		ClosableIterator it = g.find(asNode(), property, null);
		Triple old = null;
		try {
			if (it.hasNext()) {
				old = (Triple) it.next();
				if (it.hasNext()) {
					throw new SyntaxException(
						"Internal error: <"
							+ property.getURI()
							+ "> may have at most one value.");
				}
			}
		} finally {
			it.close();
		}
		if (old != null)
			g.delete(old);
        g.add(new Triple(asNode(),property,obj));
	}
    
    int getIntAttribute(Node property, int def) {
        Node obj = getAttribute(property);
        if ( obj!=null ) {
           return ((Number)obj.getLiteral().getValue()).intValue();
        }
        return def;
    }
    void setIntAttribute(Node p, int v) {
        // looks horribly inefficient :(
        setAttribute(p,Node.createLiteral(new LiteralLabel(new Integer(v))));
    }
    void incrAttribute(Node property, int diff) {
        int old = getIntAttribute(property,0);
        setIntAttribute(property,old+diff);
    }

}
