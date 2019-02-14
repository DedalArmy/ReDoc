package fr.imt.ales.redoc.type.hierarchy.structure;

public class Relation {
	JavaType endA;
	JavaType endB;
	String name;
	
	protected Relation() {}
	
	/**
	 * @param endA
	 * @param endB
	 * @param role
	 * @param name
	 */
	public Relation(JavaType endA, JavaType endB, String name) {
		super();
		this.endA = endA;
		this.endB = endB;
		this.name = name;
	}

	/**
	 * @return the endA
	 */
	public JavaType getEndA() {
		return endA;
	}

	/**
	 * @param endA the endA to set
	 */
	public void setEndA(JavaType endA) {
		this.endA = endA;
	}

	/**
	 * @return the endB
	 */
	public JavaType getEndB() {
		return endB;
	}

	/**
	 * @param endB the endB to set
	 */
	public void setEndB(JavaType endB) {
		this.endB = endB;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("\n" + endA.getFullName() + " --> " + endB.getFullName() + " : " + this.name);
		
		return str.toString();
	}	
	
	
}
