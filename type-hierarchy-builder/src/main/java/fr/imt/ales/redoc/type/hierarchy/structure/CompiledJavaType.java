package fr.imt.ales.redoc.type.hierarchy.structure;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.reflect.Parameter;

/**
 * A class for representing Java types from external compiled dependencies
 * @author Alexandre Le Borgne
 *
 */
public class CompiledJavaType extends JavaType {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7709565735073206032L;
	private static final String LIGHT_GREY = "#LightGrey";
	transient private Class<?> clazz;

	/**
	 * Parameterized constructor
	 * @param simpleName the simple name of the Java type
	 * @param jPackage the {@link JavaPackage} that is the parent of the current {@link JavaType}
	 * @param typeDeclaration the type declaration of the {@link JavaType} from the source code
	 * @param compilationUnit the parsed Java file
	 * @param clazz the {@link Class} object
	 */
	public CompiledJavaType(String simpleName, JavaPackage jPackage, TypeDeclaration<?> typeDeclaration,
			CompilationUnit compilationUnit, Class<?> clazz) {
		super(simpleName, jPackage, typeDeclaration, compilationUnit);
		this.clazz = clazz;
	}
	
	/**
	 * Parameterized constructor
	 * @param simpleName the simple name of the Java type
	 * @param jPackage the {@link JavaPackage} that is the parent of the current {@link JavaType}
	 * @param clazz the {@link Class} object
	 * @throws IOException 
	 */
	public CompiledJavaType(String simpleName, JavaPackage jPackage, Class<?> clazz) throws IOException {
		super(simpleName, jPackage, null, null);
		this.clazz = clazz;
		ArrayList<JavaType> ext = new ArrayList<JavaType>();
		ext.add(getSuperclass());
		this.setjExtends(ext);
		ArrayList<JavaType> imp = new ArrayList<JavaType>();
		ext.addAll(getInterfaces());
		this.setjImplements(imp);
	}

	/**
	 * @return the clazz
	 */
	public Class<?> getClazz() {
		return clazz;
	}
	
//	@Override
//	public String getFullName() {
//		return this.clazz.getCanonicalName();
//	}

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

	/**
	 * 
	 * @return {@code clazz} as a {@link String} if it is a class or an interface
	 */
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

	/**
	 * 
	 * @param str the {@link StringBuilder}
	 * @param method the {@link Method} to transform as a String
	 */
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

	/**
	 * 
	 * @param method the {@link Method} from which parameters are transformed to {@link String}
	 * @return {@code method} {@link Parameter}s as {@link String}
	 */
	private String writeParameters(Method method) {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < method.getParameters().length; i++) {
			if(i == 0)
				str.append(method.getParameters()[i].toString());
			else str.append(", " + method.getParameters()[i].toString());
		}
		return str.toString();
	}

	/**
	 * 
	 * @param str the {@link StringBuilder}
	 * @param field the {@link Field} to transform to {@link String}
	 */
	private void writeField(StringBuilder str, Field field) {
		switch (field.getModifiers()) {
		case Modifier.PRIVATE:
			str.append("\n\t\t-" + field.getType().getName() + " " + field.getName());
			break;
		case Modifier.PROTECTED:
			str.append("\n\t\t#" + field.getType().getName() + " " +  field.getName());
			break;
		case Modifier.PUBLIC:
			str.append("\n\t\t+" + field.getType().getName() + " " +  field.getName());
			break;
		default:
			str.append("\n\t\t~" + field.getType().getName() + " " +  field.getName());
			break;
		}
	}

	/**
	 * 
	 * @return {@code clazz} as a {@link String} if it is an enumeration
	 */
	private String writeClassEnum() {
		StringBuilder str = new StringBuilder();
		str.append("\n\tenum " + this.getFullName() + " " + LIGHT_GREY +" {");
		for(Object lit : this.clazz.getEnumConstants()) {
			str.append("\n\t\t"+lit.toString());
		}
		str.append("\n\t}");
		return str.toString();
	}
	
	@Override
	public JavaType getSuperclass() throws IOException {
		Class<?> clazz1 = this.clazz.getSuperclass();
		return clazz1!=null?this.getjPackage().addNewCompiledJavaType(clazz1):null;
	}
	
	@Override
	public List<JavaType> getInterfaces() throws IOException {
		List<JavaType> lInterfaces = new ArrayList<>();
		Class<?>[] lInter = this.clazz.getInterfaces();
		for(Class<?> inter : lInter) {
			lInterfaces.add(this.getjPackage().getCurrentHierarchyBuilder().findJavaType(inter));
		}
		return lInterfaces;
	}
	
	@Override
	public List<JavaMethod> getDeclaredMethods() {
		List<JavaMethod> declaredMethods = new ArrayList<>();
		Method[] methods = this.clazz.getDeclaredMethods();
		for(Method method : methods) {
			declaredMethods.add(new JavaMethod(method));
		}
		return declaredMethods;
	}
	
	@Override
	public JavaField getRequiredType(String refID){
		JavaField jField;
		Field cField;
		Method cMethod = null;
		try {
			cField = this.clazz.getField(refID);
			jField = new JavaField(cField.getName(), cField.getType().getName());
			return jField;
		} catch (NoSuchFieldException | SecurityException e) {
			// It could be set through a setter
			try {
				String methodName = "set" + refID.substring(0, 1).toUpperCase() + refID.substring(1);
				Method[] cMethods = this.clazz.getMethods();
				for(Method m : cMethods) {
					String name = m.getName();
					if(name.equals(methodName)) {
						cMethod = m;
						break;
					}
				}
				// The method has only one parameter which is the property to set
				if(cMethod != null && cMethod.getParameterCount() == 1) {
					jField = new JavaField(cMethod.getParameters()[0].getName(), cMethod.getParameters()[0].getType().getCanonicalName());
					return jField;
				}
				return null;
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public Boolean isAbstractType() {
		return this.clazz.isInterface() 
				|| Modifier.isAbstract(this.clazz.getModifiers());
	}

}
