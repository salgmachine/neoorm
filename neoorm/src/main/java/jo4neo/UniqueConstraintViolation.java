package jo4neo;

import org.neo4j.graphdb.Node;

import jo4neo.impl.FieldContext;

public class UniqueConstraintViolation extends IllegalStateException {

	private FieldContext ctx;

	private Node n;

	public void setContext(FieldContext ctx) {
		this.ctx = ctx;
	}

	public void setNode(Node n) {
		this.n = n;
	}

	@Override
	public String getMessage() {
		return "Unique Constraint Violation on Field " + ctx.getFieldname()
				+ " with Value " + ctx.value() + " on Node Id " + n.getId();
	}
}
