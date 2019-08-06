package fr.imt.ales.redoc.type.hierarchy.structure;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * A class for representing nested Java types
 * @author Alexandre Le Borgne
 *
 */
public class JavaNestedType extends JavaType {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1798467335449864299L;
	private JavaType parent;

	/**
	 * Parameterized constructor
	 * @param simpleName the simple name of the Java type
	 * @param jPackage the {@link JavaPackage} that is the parent of the current {@link JavaType}
	 * @param typeDeclaration the type declaration of the {@link JavaType} from the source code
	 * @param compilationUnit the parsed Java file
	 * @param parent the parent {@link JavaType}
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
