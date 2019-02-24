package fr.imt.ales.redoc.type.hierarchy.structure;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * A class for representing Java types
 * @author Alexandre Le Borgne
 *
 */
public class JavaType {
	/*
	 * ATTRIBUTES
	 */
	/**
	 * Simple name of the {@link JavaType}
	 */
	private String simpleName;
	/**
	 * The parent {@link JavaPackage}
	 */
	private JavaPackage jPackage;
	/**
	 * The {@link TypeDeclaration} of the {@link JavaType}
	 */
	private TypeDeclaration<?> typeDeclaration;
	/**
	 * The {@link CompilationUnit} corresponding to the Java file of the {@link JavaType}
	 */
	private CompilationUnit compilationUnit;
	/**
	 * Imported {@link JavaType}s
	 */
	private List<JavaType> jImports;
	/**
	 * Extended {@link JavaType}s
	 */
	private List<JavaType> jExtends;
	/**
	 * Implemented {@link JavaType}s
	 */
	private List<JavaType> jImplements;
	/**
	 * Nested {@link JavaType}s
	 */
	private List<JavaNestedType> nestedTypes;

	/**
	 * Parameterized constructor
	 * @param simpleName the simple name of the Java type
	 * @param jPackage the {@link JavaPackage} that is the parent of the current {@link JavaType}
	 * @param typeDeclaration the type declaration of the {@link JavaType} from the source code
	 * @param compilationUnit the parsed Java file
	 */
	public JavaType(String simpleName, JavaPackage jPackage, TypeDeclaration<?> typeDeclaration, CompilationUnit compilationUnit) {
		super();
		this.simpleName = simpleName;
		this.jPackage = jPackage;
		this.jPackage.addJavaType(this);
		this.typeDeclaration = typeDeclaration;
		this.compilationUnit = compilationUnit;
		this.jImports = new ArrayList<>();
		this.jExtends = new ArrayList<>();
		this.jImplements = new ArrayList<>();
		this.nestedTypes = new ArrayList<>();
	}


	/**
	 * @return the simpleName
	 */
	public String getSimpleName() {
		return simpleName;
	}

	/**
	 * @param simpleName the simpleName to set
	 */
	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return "".equals(this.jPackage.getName())?this.simpleName : jPackage.getName()+'.'+this.simpleName;
	}

	/**
	 * @return the jPackage
	 */
	public JavaPackage getjPackage() {
		return jPackage;
	}

	/**
	 * @param jPackage the jPackage to set
	 */
	public void setjPackage(JavaPackage jPackage) {
		this.jPackage = jPackage;
	}

	/**
	 * @return the typeDeclaration
	 */
	public TypeDeclaration<?> getTypeDeclaration() {
		return typeDeclaration;
	}

	/**
	 * @param typeDeclaration the typeDeclaration to set
	 */
	public void setTypeDeclaration(TypeDeclaration<?> typeDeclaration) {
		this.typeDeclaration = typeDeclaration;
	}

	/**
	 * @return the jImports
	 */
	public List<JavaType> getjImports() {
		return jImports;
	}

	/**
	 * @param jImports the jImports to set
	 */
	public void setjImports(List<JavaType> jImports) {
		this.jImports = jImports;
	}

	/**
	 * @return the jExtends
	 */
	public List<JavaType> getjExtends() {
		return jExtends;
	}

	/**
	 * @param jExtends the jExtends to set
	 */
	public void setjExtends(List<JavaType> jExtends) {
		this.jExtends = jExtends;
	}

	/**
	 * @return the compilationUnit
	 */
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	/**
	 * @param compilationUnit the compilationUnit to set
	 */
	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	/**
	 * @return the nestedTypes
	 */
	public List<JavaNestedType> getNestedTypes() {
		return nestedTypes;
	}

	/**
	 * @param nestedTypes the nestedTypes to set
	 */
	public void setNestedTypes(List<JavaNestedType> nestedTypes) {
		this.nestedTypes = nestedTypes;
	}

	/**
	 * @return the jImplements
	 */
	public List<JavaType> getjImplements() {
		return jImplements;
	}

	/**
	 * @param jImplements the jImplements to set
	 */
	public void setjImplements(List<JavaType> jImplements) {
		this.jImplements = jImplements;
	}

	/*
	 * METHODS
	 */

	/**
	 * This method adds a {@link JavaType} to {@code jImport}
	 * @param jImport {@link JavaType} to add
	 */
	public void addImport(JavaType jImport) {
		this.jImports.add(jImport);
	}	

	/**
	 * This method adds a {@link JavaType} to {@code jExtends}
	 * @param jExt {@link JavaType} to add
	 */
	public void addExtends(JavaType jExt) {
		this.jExtends.add(jExt);
	}

	/**
	 * This method adds a {@link JavaType} to {@code nestedTypes}
	 * @param nested {@link JavaType} to add
	 */
	public void addNestedType(JavaNestedType nested) {
		this.nestedTypes.add(nested);
	}

	/**
	 * @return a plantuml based String description of the {@link JavaType}
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if(this.typeDeclaration.isClassOrInterfaceDeclaration())
		{
			str.append(this.writeClassOrInterface());
		} else if(this.typeDeclaration.isEnumDeclaration())
		{
			str.append(this.writeEnum());
		}
		return str.toString();
	}
	
	/**
	 * 
	 * @return {@code typeDeclaration} as a {@link String}
	 */
	String writeClassOrInterface() {
		StringBuilder str = new StringBuilder();
		str.append("\n\t");
		ClassOrInterfaceDeclaration tempCOID = this.typeDeclaration.asClassOrInterfaceDeclaration();
		writeCOID(str, tempCOID);

		for(FieldDeclaration field : tempCOID.getFields())
		{
			str.append(this.writeField(field));
		}
		for(MethodDeclaration method : tempCOID.getMethods())
		{
			str.append(this.writeMethod(method));
		}
		str.append("\n\t}");
		return str.toString();
	}

	/**
	 * @param str the {@link StringBuilder}
	 * @param tempCOID the {@link ClassOrInterfaceDeclaration} being written
	 */
	void writeCOID(StringBuilder str, ClassOrInterfaceDeclaration tempCOID) {
		if(tempCOID.isInterface())
		{
			str.append("interface " + this.getFullName() + " {");
		} else {
			if(tempCOID.isAbstract())
				str.append("abstract ");
			str.append("class " + this.getFullName() + " {");
		}
	}
	
	/**
	 * 
	 * @return {@code typeDeclaration} as a {@link String} if it is an {@link EnumDeclaration}
	 */
	String writeEnum() {
		StringBuilder str = new StringBuilder();
		EnumDeclaration tempEnum = this.typeDeclaration.asEnumDeclaration();
		str.append("\n\tenum " + this.getFullName() + " {");
		for(EnumConstantDeclaration lit : tempEnum.getEntries())
		{
			str.append("\n\t\t"+lit.toString());
		}
		str.append("\n\t}");
		return str.toString();
	}

	/**
	 * 
	 * @param field the {@link FieldDeclaration}
	 * @return {@code field} as a {@link String}
	 */
	String writeField(FieldDeclaration field) {
		StringBuilder str = new StringBuilder();

		if(field.isPublic())
			str.append("\n\t\t+" + field.getElementType().asString() + " " + field.getVariable(0).getNameAsString());
		else if(field.isPrivate())
			str.append("\n\t\t-" + field.getElementType().asString() + " " + field.getVariable(0).getNameAsString());
		else if(field.isProtected())
			str.append("\n\t\t#" + field.getElementType().asString() + " " + field.getVariable(0).getNameAsString());
		else str.append("\n\t\t~" + field.getElementType().asString() + " " + field.getVariable(0).getNameAsString());

		return str.toString();
	}

	/**
	 * 
	 * @param method the {@link MethodDeclaration}
	 * @return {@code method} as a {@link String}
	 */
	String writeMethod(MethodDeclaration method) {
		StringBuilder str = new StringBuilder();
		if(method.isPublic())
			str.append("\n\t\t+" + method.getTypeAsString() + " " + method.getSignature().asString());
		else if(method.isPrivate())
			str.append("\n\t\t-" + method.getTypeAsString() + " " + method.getSignature().asString());
		else if(method.isProtected())
			str.append("\n\t\t#" + method.getTypeAsString() + " " + method.getSignature().asString());
		else str.append("\n\t\t~" + method.getTypeAsString() + " " + method.getSignature().asString());

		return str.toString();
	}
}
