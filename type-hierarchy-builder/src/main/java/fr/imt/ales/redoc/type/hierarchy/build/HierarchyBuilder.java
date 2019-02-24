package fr.imt.ales.redoc.type.hierarchy.build;
/**
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import fr.imt.ales.redoc.jarloader.JarLoader;
import fr.imt.ales.redoc.type.hierarchy.build.explorer.Explorer;
import fr.imt.ales.redoc.type.hierarchy.structure.CompiledJavaType;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaNestedType;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaPackage;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;
import fr.imt.ales.redoc.type.hierarchy.structure.ParameterizedJavaType;
import fr.imt.ales.redoc.type.hierarchy.structure.Relation;

/**
 * @author Alexandre Le Borgne
 *
 */
public class HierarchyBuilder {

	/*
	 * LOGGER
	 */
	static final Logger logger = LogManager.getLogger(HierarchyBuilder.class);

	/*
	 * ATTRIBUTES
	 */
	static final String JAVA_EXTENSION = ".java";

	private static final String JAVA_LANG = "java.lang";
	private List<File> javaFiles;
	private List<CompilationUnit> compilationUnits;
	private List<JavaPackage> packages;
	private JarLoader jarLoader;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Default constructor
	 */
	public HierarchyBuilder() {
		this.javaFiles = new ArrayList<>();
		this.packages = new ArrayList<>();
		this.compilationUnits=new ArrayList<>();
		this.jarLoader = null;
	}

	/**
	 * Constructor that automatically loads the Java files contained into a folder identified by {@code path}
	 * @param path the path that identifies the Java project folder
	 * @throws IOException 
	 */
	public HierarchyBuilder(String path, String ... dependencyPaths) throws IOException {
		this.javaFiles = Explorer.getFiles(path, JAVA_EXTENSION);
		this.packages = new ArrayList<>();
		this.compilationUnits=new ArrayList<>();
		this.jarLoader = new JarLoader(getUnion(path, dependencyPaths));
	}

	/*
	 * GETTERS AND SETTERS
	 */

	/**
	 * 
	 * @param path
	 * @param dependencyPaths
	 * @return
	 */
	private String[] getUnion(String path, String[] dependencyPaths) {
		int length = dependencyPaths.length;
		if(length > 0) {
			String[] union = new String[length +1];
			union[0] = path;
			for(int i = 0; i<length; i++) {
				union[i+1] = dependencyPaths[i];
			}
			return union;
		} else return new String[] {path};
	}

	/**
	 * @return the javaFiles
	 */
	public List<File> getJavaFiles() {
		return javaFiles;
	}

	/**
	 * @param javaFiles the javaFiles to set
	 */
	public void setJavaFiles(List<File> javaFiles) {
		this.javaFiles = javaFiles;
	}

	/**
	 * @return the compilationUnits
	 */
	public List<CompilationUnit> getCompilationUnits() {
		return compilationUnits;
	}

	/**
	 * @param compilationUnits the compilationUnits to set
	 */
	public void setCompilationUnits(List<CompilationUnit> compilationUnits) {
		this.compilationUnits = compilationUnits;
	}

	/**
	 * @return the packages
	 */
	public List<JavaPackage> getPackages() {
		return packages;
	}

	/**
	 * @param packages the packages to set
	 */
	public void setPackages(List<JavaPackage> packages) {
		this.packages = packages;
	}

	/*
	 * METHODS
	 */

	/**
	 * Loads the Java files contained into a directory
	 * @param path the path that identifies the directory
	 */
	public void loadJavaFiles(String path) {
		this.javaFiles = Explorer.getFiles(path, JAVA_EXTENSION);
	}

	/**
	 * This methods builds the hierarchy from the classes that have been found into the source code.
	 */
	public void build() {
		if(!this.javaFiles.isEmpty())
		{
			this.javaFiles.forEach(f -> {
				try {
					this.compilationUnits.add(JavaParser.parse(f));
				} catch (FileNotFoundException e) {
					logger.error("A problem occured while parsing a Java file : " + f.getAbsolutePath(),e);
				}
				logger.debug(f.getAbsolutePath() + " has been successfuly parsed.");
			});
			if(!this.compilationUnits.isEmpty())
			{
				this.exploreHierarchy();
				this.packages.forEach(logger::trace);
				this.exploreRelations();
			}
			else logger.warn("The list of Java compilation units is empty and then the hierarchy construction cannot go further.");
		}
		else logger.warn("The list of Java files is empty.");
	}

	/**
	 * This methods explores and extracts the type hierarchy from the project
	 */
	private void exploreHierarchy() {
		this.loadNecessaryData();
		int length = getPackages().size();
		for(int i = 0; i < length; i++)
		{
			JavaPackage pack = getPackages().get(i);
			for(JavaType type : pack.getJavaTypes())
			{
				type.setjImports(this.getImportedJavaTypes(type));
				type.setjExtends(this.getExtendedJavaTypes(type));
				type.setjImplements(this.getImplementedJavaTypes(type));
			}
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private List<JavaType> getImportedJavaTypes(JavaType type) {
		List<JavaType> result = new ArrayList<>();
		for(ImportDeclaration imp : type.getCompilationUnit().getImports() ) {
			if(imp.isAsterisk())
			{
				for(JavaPackage pack : packages)
				{
					if(pack.getName().equals(imp.getNameAsString()))
						result.addAll(pack.getJavaTypes());
				}
			} else {
				for(JavaPackage pack : packages)
				{
					JavaType jType = pack.findTypeByName(imp.getNameAsString());
					if(jType!=null)
						result.add(jType);
				}
			}			
		}
		return result;
	}

	/**
	 * This method sets packageNameToJavaTypeNames and standardizedJavaNameToCompilationUnit.
	 * It also extracts ClassesOrInterface declarations from compilation units and stores them into {@code classOrInterfaceDeclarations}.
	 */
	private void loadNecessaryData() {
		for(CompilationUnit cu : this.compilationUnits)
		{
			String typeName = null;
			Optional<PackageDeclaration> packageDeclaration = cu.getPackageDeclaration();
			String packageName = (packageDeclaration.isPresent())?packageDeclaration.get().getNameAsString():"";

			JavaPackage jPackage = this.getPackage(packageName);
			NodeList<TypeDeclaration<?>> types = cu.getTypes();
			for(TypeDeclaration<?> type : types)
			{
				JavaType javaType = null;
				typeName = type.getNameAsString();
				if(type.isClassOrInterfaceDeclaration())
				{
					ClassOrInterfaceDeclaration coid = type.asClassOrInterfaceDeclaration();
					if(coid.getTypeParameters().isEmpty())
						javaType = new JavaType(typeName, jPackage, coid, cu);
					else javaType = new ParameterizedJavaType(typeName, jPackage, coid, cu);
					for(BodyDeclaration<?> m : coid.getMembers())
					{
						if(m.isTypeDeclaration())
						{
							JavaNestedType javaNestedType = new JavaNestedType(m.asTypeDeclaration().getNameAsString(), jPackage, m.asTypeDeclaration(), cu, javaType);
							javaType.addNestedType(javaNestedType);
						}
					}
				} else {
					new JavaType(typeName, jPackage, type, cu);
				}
			}
		}
	}

	/**
	 * This method returns the JavaPackage named {@code packageName} if it exists {@code packages} or a new package with the name {@code packageName} if it does not.
	 * @param packageName
	 * @return the package
	 */
	private JavaPackage getPackage(String packageName) {
		for(JavaPackage pack : getPackages())
		{
			if(pack.getName().equals(packageName))
				return pack;
		}
		JavaPackage pack = new JavaPackage(packageName);
		this.packages.add(pack);
		return pack;
	}

	/**
	 * This method returns the list of {@link JavaType} that correspond to extended types of {@code type}.
	 * @param type {@link JavaType} which is investigated.
	 * @return the list of {@link JavaType} that correspond to extended types of {@code type}.
	 */
	private List<JavaType> getExtendedJavaTypes(JavaType type) {
		List<JavaType> result = new ArrayList<>();

		TypeDeclaration<?> typeDeclaration = type.getTypeDeclaration();
		if(typeDeclaration.isClassOrInterfaceDeclaration())
		{
			NodeList<ClassOrInterfaceType> extendedTypes = typeDeclaration.asClassOrInterfaceDeclaration().getExtendedTypes();
			for(ClassOrInterfaceType et : extendedTypes)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug(type.getSimpleName() + " --|> " + et);
				}
				JavaType coi = findClassOrInterface(type, et);
				if(coi != null)
					result.add(coi);
				else logger.warn("Missing dependency : " + et.asString());
			}
		}
		return result;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private List<JavaType> getImplementedJavaTypes(JavaType type) {
		List<JavaType> result = new ArrayList<>();

		TypeDeclaration<?> typeDeclaration = type.getTypeDeclaration();
		if(typeDeclaration.isClassOrInterfaceDeclaration())
		{
			NodeList<ClassOrInterfaceType> extendedTypes = typeDeclaration.asClassOrInterfaceDeclaration().getImplementedTypes();
			for(ClassOrInterfaceType et : extendedTypes)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug(type.getSimpleName() + " ..|> " + et);
				}
				JavaType coi = findClassOrInterface(type, et);
				if(coi != null)
					result.add(coi);
				else logger.warn("Missing dependency : " + et.asString());
			}
		}
		return result;
	}

	/**
	 * This method explores and extracts relations between types
	 */
	private void exploreRelations() {
		int length = getPackages().size();
		for(int i = 0; i < length; i++)
		{
			JavaPackage pack = getPackages().get(i);
			for(JavaType type : pack.getJavaTypes())
			{
				if(type instanceof CompiledJavaType) {
					CompiledJavaType cjt = ((CompiledJavaType)type);
					for(Field fd : cjt.getClazz().getFields()) {
						exploreRelations(pack, type, fd);
					}
				} else if(type instanceof JavaType) {
					for(FieldDeclaration fd : type.getTypeDeclaration().getFields())
					{
						exploreRelations(pack, type, fd);
					}
				}
			}
		}
	}

	private void exploreRelations(JavaPackage pack, JavaType type, Field fd) {
		logger.debug("External reference." + fd.getType().getCanonicalName());
	}

	/**
	 * @param pack
	 * @param type
	 * @param fd
	 */
	private void exploreRelations(JavaPackage pack, JavaType type, FieldDeclaration fd) {
		if(fd.getElementType().isClassOrInterfaceType())
		{
			JavaType tempJavaType = this.findClassOrInterface(type, fd.getElementType().asClassOrInterfaceType());
			if(tempJavaType!=null)
			{
				pack.addRelation(new Relation(type, tempJavaType, fd.getVariable(0).getNameAsString()));
			} else { //Paramaterized types
				ClassOrInterfaceType parameterType = fd.getElementType().asClassOrInterfaceType();
				Optional<NodeList<Type>> typeArguments = parameterType.asClassOrInterfaceType().getTypeArguments();
				if(typeArguments.isPresent() && !typeArguments.get().isEmpty())
				{
					this.computeParameterTypes(typeArguments.get()).forEach(ta -> {
						JavaType tempJType = this.findClassOrInterface(type, ta.asClassOrInterfaceType());
						if(tempJType!=null)
						{
							pack.addRelation(new Relation(type, tempJType, fd.getVariable(0).getNameAsString()));
						}
					});
				}
			}					
		}
	}

	/**
	 * 
	 * @param nodeList
	 * @return
	 */
	private List<Type> computeParameterTypes(NodeList<Type> nodeList){
		List<Type> result = new ArrayList<>();
		for(Type type : nodeList)
		{
			if(type.isClassOrInterfaceType())
			{
				Optional<NodeList<Type>> typeArguments = type.asClassOrInterfaceType().getTypeArguments();
				if(typeArguments.isPresent() && !typeArguments.get().isEmpty())
				{
					result.addAll(this.computeParameterTypes(typeArguments.get()));
				} else result.add(type);
			}

		}
		return result;
	}

	/**
	 * This method finds the ClassOrInterfaceDeclaration ({@code coid}) corresponding to {@code coi}.
	 * @param type Current compilation unit.
	 * @param coi Class or Interface type to find
	 */
	private JavaType findClassOrInterface(JavaType type, ClassOrInterfaceType coi) {
		String name = coi.getName().toString();
		/*
		 * LOCAL DEPENDENCIES
		 */
		for(JavaType imp : type.getjImports()) {
			if(imp.getSimpleName().equals(name) || imp.getFullName().equals(name))
				return imp;
		}
		// If still into the method, the dependency is not into the imports it is maybe into the same package
		JavaType tempJavaType = type.getjPackage().findTypeByName(name);
		if(tempJavaType!=null)
		{
			return tempJavaType;
		}
		//If still running, then it could be an explicit type (fr.package.name.ClassName)
		for(JavaPackage pack : this.packages)
		{
			tempJavaType = pack.findTypeByName(name);
			if(tempJavaType!=null)
				return tempJavaType;
		}

		/*
		 * EXTERNAL DEPENDENCIES
		 */

		return findExternalClassOrInterface(type, coi);
	}

	private JavaType findExternalClassOrInterface(JavaType type, ClassOrInterfaceType coi) {

		logger.debug("External ---------------------------------------> "+coi.asString());

		JavaType result = null;

		/*
		 * FIND IN DEFAULT PACKAGES
		 */
		result = findTypeInDefaultPackages(type, coi);
		if(result != null)
			return result;	

		/*
		 * FIND IN IMPORTS
		 */
		result = findTypeInImports(type, coi);
		return (result != null)? result : null;
	}

	/**
	 * 
	 * @param type
	 * @param coi
	 * @return
	 * @throws ClassNotFoundException
	 */
	private JavaType findTypeInDefaultPackages(JavaType type, ClassOrInterfaceType coi) {
		String name = coi.getName().toString();
		int index = this.jarLoader.getClassNames().indexOf(name);
		if(index>=0) { // The class name is explicit (a.b.c.ClassName)
			return getJavaType(index);
		} else {
			index = this.jarLoader.getClassNames().indexOf(type.getjPackage().getName()+"."+name);
			if(index>=0) { // The class is located into the same package
				return getJavaType(index);
			}
			else {
				String cName = JAVA_LANG+"."+name;
				try { // The class is located into java.lang package
					Class<?> clazz = Class.forName(cName);
					String simpleName = clazz.getSimpleName();
					String packageName = clazz.getPackageName();
					return new CompiledJavaType(simpleName, this.getPackage(packageName), null, null, clazz);
				}catch(ClassNotFoundException e) {
					// The class is not located into java.lang
					return null;
				}
			}
		}
	}

	/**
	 * @param result
	 * @param index
	 * @return
	 * @throws ClassNotFoundException
	 */
	private JavaType getJavaType(int index) {
		Class<?> clazz;
		try {
			clazz = this.jarLoader.loadClass(this.jarLoader.getClassNames().get(index));
			return new CompiledJavaType(clazz.getSimpleName(), this.getPackage(clazz.getPackageName()), null, null, clazz);
		} catch (ClassNotFoundException e) {
			if(logger.isErrorEnabled())
				logger.warn(e.getCause());
			return null;
		}
	}



	private JavaType findTypeInImports(JavaType type, ClassOrInterfaceType coi) {
		NodeList<ImportDeclaration> imports = type.getCompilationUnit().getImports();
		for(ImportDeclaration imp : imports) {
			String name = coi.getName().toString();
			if(imp.isAsterisk()) { // It could be the package that contains the class we want
				List<String> classNames = this.jarLoader.getPackageNameToClassNames().get(imp.getNameAsString());
				if(classNames!=null) {
					for(String cn : classNames) {
						if(cn.endsWith(name)) { // This is the class that must be loaded
							try { // This is the class that must be loaded
								Class<?> clazz = this.jarLoader.loadClass(cn);
								String simpleName = clazz.getSimpleName();
								String packageName = clazz.getPackageName();
								return new CompiledJavaType(simpleName, this.getPackage(packageName), null, null, clazz);
							}catch(ClassNotFoundException e) {
								// This is a missing dependency
								return null;
							}
						}
					}
				}
			} else if(imp.getNameAsString().endsWith(name)){
				try { // This is the class that must be loaded
					Class<?> clazz = this.jarLoader.loadClass(imp.getNameAsString());
					String simpleName = clazz.getSimpleName();
					String packageName = clazz.getPackageName();
					return new CompiledJavaType(simpleName, this.getPackage(packageName), null, null, clazz);
				}catch(ClassNotFoundException e) {
					// This is a missing dependency
					return null;
				}
			}
		}
		return null;
	}
}