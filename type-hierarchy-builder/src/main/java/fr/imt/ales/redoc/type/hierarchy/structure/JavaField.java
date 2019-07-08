package fr.imt.ales.redoc.type.hierarchy.structure;

public class JavaField {
	
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
