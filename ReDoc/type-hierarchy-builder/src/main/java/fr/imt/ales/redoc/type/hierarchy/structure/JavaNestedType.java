package fr.imt.ales.redoc.type.hierarchy.structure;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

public class JavaNestedType extends JavaType {
	private JavaType parent;

	/**
	 * @param simpleName
	 * @param jPackage
	 * @param typeDeclaration
	 * @param parent
	 */
	public JavaNestedType(String simpleName, JavaPackage jPackage, TypeDeclaration<?> typeDeclaration, CompilationUnit compilationUnit,
			JavaType parent) {
		super(simpleName, jPackage, typeDeclaration, compilationUnit);
		this.parent = parent;
	}

	/**
	 * @return the parent
	 */
	public JavaType getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(JavaType parent) {
		this.parent = parent;
	}
	
	
}
