package fr.imt.ales.redoc.type.hierarchy.structure;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * A class to represent Java package structure
 * @author Alexandre Le Borgne
 *
 */
public class JavaPackage {
	/*
	 * ATTRIBUTES
	 */
	/**
	 * The name of the package
	 */
	private String name = "";
	/**
	 * The {@link List} of {@link JavaType}s in the package
	 */
	private List<JavaType> javaTypes;
	/**
	 * The {@link List} of {@link Relation}s of the package
	 */
	private List<Relation> relations;

	/*
	 * CONSTRUCTOR
	 */
	/**
	 * Parameterized constructor
	 * @param name the name of the package
	 */
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
	 * Finds a {@link JavaType} into the package by its name
	 * @param name can be the simple name or the full name of the {@link JavaType}
	 * @return the corresponding {@link JavaType} if it exists or <code>null</code> otherwise
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
	 * Finds a {@link JavaType} into the package by its {@link TypeDeclaration}
	 * @param typeDeclaration the {@link TypeDeclaration} of the {@link JavaType} that is required
	 * @return the corresponding {@link JavaType} if it exists or <code>null</code> otherwise
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
	 * Adds a {@link JavaType} to {@code javaTypes}
	 * @param type the {@link JavaType} to add
	 */
	public void addJavaType(JavaType type) {
		this.javaTypes.add(type);
	}
	
	/**
	 * Adds a {@link Relation} to {@code javaTypes}
	 * @param relation {@link Relation} to add
	 */
	public void addRelation(Relation relation) {
		this.relations.add(relation);
	}
		
	/**
	 * @return a plantuml based String description of the {@link JavaPackage}
	 */
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
