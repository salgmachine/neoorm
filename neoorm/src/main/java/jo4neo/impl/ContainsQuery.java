package jo4neo.impl;

import jo4neo.ObjectGraph;
import jo4neo.fluent.Contains;
import jo4neo.fluent.Result;

public class ContainsQuery<T> implements Contains<T> {

	ObjectGraph pm;
	Class<T> c;
	FieldContext f;

	public ContainsQuery(FieldContext f, ObjectGraph pm, Class<T> c) {
		this.pm = pm;
		this.c = c;
		this.f = f;
	}

	@Override
	public Result<T> contains(Object o) {
		return new ContainsResultImpl<T>(pm, c, f.getIndexName(), o);
	}
}
