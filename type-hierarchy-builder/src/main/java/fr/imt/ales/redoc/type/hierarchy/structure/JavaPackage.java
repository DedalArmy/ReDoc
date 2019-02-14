package fr.imt.ales.redoc.type.hierarchy.structure;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.TypeDeclaration;

public class JavaPackage {
	
	private String name = "";
	private List<JavaType> javaTypes;
	private List<Relation> relations;

	public JavaPackage(String name) {
		super();
		this.name = name;
		javaTypes = new ArrayList<>();
		relations = new ArrayList<>();
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

	/**
	 * @return the javaTypes
	 */
	public List<JavaType> getJavaTypes() {
		return javaTypes;
	}

	/**
	 * @param javaTypes the javaTypes to set
	 */
	public void setJavaTypes(List<JavaType> javaTypes) {
		this.javaTypes = javaTypes;
	}

	/**
	 * @return the relations
	 */
	public List<Relation> getRelations() {
		return relations;
	}

	/**
	 * @param relations the relations to set
	 */
	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}
	
	/*
	 * METHODS
	 */
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public JavaType findTypeByName(String name){
		for(JavaType type : this.javaTypes)
		{
			if(type.getSimpleName().equals(name) || type.getFullName().equals(name))
				return type;
			else { //It can be a nested type
				for(JavaNestedType nested : type.getNestedTypes())
				{
					if(nested.getSimpleName().equals(name) || nested.getFullName().equals(name))
						return nested;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param typeDeclaration
	 * @return
	 */
	public JavaType findByTypeDeclaration(TypeDeclaration<?> typeDeclaration) {
		for(JavaType type : this.javaTypes)
		{
			if(type.getTypeDeclaration().equals(typeDeclaration))
				return type;
			else { //It can be a nested type
				for(JavaNestedType nested : type.getNestedTypes())
				{
					if(nested.getTypeDeclaration().equals(typeDeclaration))
						return nested;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param type
	 */
	public void addJavaType(JavaType type) {
		this.javaTypes.add(type);
	}
	
	/**
	 * 
	 * @param relation
	 */
	public void addRelation(Relation relation) {
		this.relations.add(relation);
	}
		
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("\npackage " + (("".equals(this.name))?"_default_": this.name) +" {");
		for(JavaType jt : this.javaTypes)
		{
			str.append(jt);
		}
		str.append("\n}\n");
		return str.toString();
	}
}
