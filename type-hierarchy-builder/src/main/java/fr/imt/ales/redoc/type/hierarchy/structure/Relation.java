package fr.imt.ales.redoc.type.hierarchy.structure;

import java.io.Serializable;

/**
 * A class for representing relation between {@link JavaType}s
 * @author Alexandre Le Borgne
 *
 */
public class Relation implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8724403938470507641L;
	JavaType endA;
	JavaType endB;
	String name;
	
	/**
	 * protected default constructor for avoiding default instantiation from outside the package
	 */
	protected Relation() {}
	
	/**
	 * Parameterized constructor
	 * @param endA source of the relation 
	 * @param endB target of the relation
	 * @param name the name of the relation
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
	/**
	 * @return a plantuml based String description of the {@link Relation}
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("\n" + endA.getFullName() + " --> " + endB.getFullName() + " : " + this.name);
		
		return str.toString();
	}	
	
	
}
