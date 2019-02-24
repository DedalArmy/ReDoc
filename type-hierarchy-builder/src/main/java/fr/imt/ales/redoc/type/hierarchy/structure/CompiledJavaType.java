package fr.imt.ales.redoc.type.hierarchy.structure;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

public class CompiledJavaType extends JavaType {

	private static final String LIGHT_GREY = "#LightGrey";
	private Class<?> clazz;

	public CompiledJavaType(String simpleName, JavaPackage jPackage, TypeDeclaration<?> typeDeclaration,
			CompilationUnit compilationUnit, Class<?> clazz) {
		super(simpleName, jPackage, typeDeclaration, compilationUnit);
		this.clazz = clazz;
	}

	/**
	 * @return the clazz
	 */
	public Class<?> getClazz() {
		return clazz;
	}
	
	@Override
	public String getFullName() {
		return this.clazz.getCanonicalName();
	}

	/**
	 * @param clazz the clazz to set
	 */
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		if(this.clazz.isEnum())
			str.append(this.writeClassEnum());
		else {
			str.append(this.writeClassClassOrInterface());
		}
		return str.toString();
	}

	private String writeClassClassOrInterface() {
		StringBuilder str = new StringBuilder();
		str.append("\n\t");
		if(this.clazz.isInterface())
			str.append("interface " + this.getFullName() + " " + LIGHT_GREY + " {");
		else if(Modifier.isAbstract(this.clazz.getModifiers()))
			str.append("abstract class " + this.getFullName() + " " + LIGHT_GREY + " {");
		else
			str.append("class " + this.getFullName() + " " + LIGHT_GREY + " {");
		for(Field field : this.clazz.getFields()) {
			this.writeField(str, field);
		}
		for(Method method : this.clazz.getMethods()) {
			this.writeMethod(str, method);
		}
		str.append("\n\t}");
		return str.toString();
	}

	private void writeMethod(StringBuilder str, Method method) {
		switch (method.getModifiers()) {
		case Modifier.PRIVATE:
			str.append("\n\t\t-" + method.getReturnType().getName() + " " + method.getName() + "(" + this.writeParameters(method) + ")");
			break;
		case Modifier.PROTECTED:
			str.append("\n\t\t#" + method.getReturnType().getName() + " " + method.getName() + "(" + this.writeParameters(method) + ")");
			break;
		case Modifier.PUBLIC:
			str.append("\n\t\t+" + method.getReturnType().getName() + " " + method.getName() + "(" + this.writeParameters(method) + ")");
			break;
		default:
			str.append("\n\t\t~" + method.getReturnType().getName() + " " + method.getName() + "(" + this.writeParameters(method) + ")");
			break;
		}
	}

	private String writeParameters(Method method) {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < method.getParameters().length; i++) {
			if(i == 0)
				str.append(method.getParameters()[i].toString());
			else str.append(", " + method.getParameters()[i].toString());
		}
		return str.toString();
	}

	private void writeField(StringBuilder str, Field field) {
		switch (field.getModifiers()) {
		case Modifier.PRIVATE:
			str.append("\n\t\t-" + field.getType().getName() + " " + field.getName());
			break;
		case Modifier.PROTECTED:
			str.append("\n\t\t#" + field.getType().getName() + field.getName());
			break;
		case Modifier.PUBLIC:
			str.append("\n\t\t+" + field.getType().getName() + field.getName());
			break;
		default:
			str.append("\n\t\t~" + field.getType().getName() + field.getName());
			break;
		}
	}

	private String writeClassEnum() {
		StringBuilder str = new StringBuilder();
		str.append("\n\tenum " + this.getFullName() + " " + LIGHT_GREY +" {");
		for(Object lit : this.clazz.getEnumConstants()) {
			str.append("\n\t\t"+lit.toString());
		}
		str.append("\n\t}");
		return str.toString();
	}

}
