package jo4neo.impl;

public class UniqueConstraintViolation extends IllegalStateException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3183147501427096481L;

	private String fieldname;

	private Object value;

	@Override
	public String getMessage() {
		String msg = "Unique Constraint Violation for Field " + fieldname
				+ " with Value " + value.toString();

		StringBuffer b = new StringBuffer();
		b.append(msg).append("\r\n");
		for (StackTraceElement e : super.getStackTrace()) {
			b.append("\tat: ").append(e.toString()).append("\r\n");
		}

		return b.toString();
	}

	public String getFieldname() {
		return fieldname;
	}

	public void setFieldname(String fieldname) {
		this.fieldname = fieldname;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
