package fr.imt.ales.redoc.type.hierarchy.structure;

import java.io.Serializable;

public class JavaField implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6865947420273526746L;
	String name;
	String type;

	public JavaField(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

}
