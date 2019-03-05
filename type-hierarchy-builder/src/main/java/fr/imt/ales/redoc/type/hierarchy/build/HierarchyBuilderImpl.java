package fr.imt.ales.redoc.type.hierarchy.build;
/**
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * A class for re-documenting the type hierarchy of a Java project, considering source code and compiled dependencies
 * @author Alexandre Le Borgne
 */
public class HierarchyBuilderImpl implements HierarchyBuilder {

	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	static final Logger logger = LogManager.getLogger(HierarchyBuilderImpl.class);

	/*
	 * ATTRIBUTES
	 */
	/**
	 * Java file extension
	 */
	static final String JAVA_EXTENSION = ".java";
	/**
	 * java.lang package
	 */
	private static final String JAVA_LANG = "java.lang";
	/**
	 * Jar file extension
	 */
	private List<File> javaFiles;
	/**
	 * {@link List} of {@link CompilationUnit}
	 */
	private List<CompilationUnit> compilationUnits;
	/**
	 * {@link List} of {@link JavaPackage}
	 */
	private List<JavaPackage> packages;
	/**
	 * For loading jar/war archives and analyze compiled dependencies
	 */
	private JarLoader jarLoader;

	private String path;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Default constructor
	 */
	protected HierarchyBuilderImpl() {
		this.javaFiles = new ArrayList<>();
		this.packages = new ArrayList<>();
		this.compilationUnits=new ArrayList<>();
		this.jarLoader = null;
	}

	/**
	 * Constructor that automatically loads the Java files contained into a folder identified by {@code path}
	 * @param path the path that identifies the Java project folder
	 * @param dependencyPaths the paths that identify the dependency folders
	 * @throws IOException  if an I/O error occurs when opening the directory
	 */
	protected HierarchyBuilderImpl(String path, String ... dependencyPaths) throws IOException  {
		this.setPath(path);
		this.javaFiles = Explorer.getFiles(path, JAVA_EXTENSION);
		this.packages = new ArrayList<>();
		this.compilationUnits=new ArrayList<>();
		this.jarLoader = new JarLoader(this.getUnion(path, dependencyPaths));
	}

	/*
	 * GETTERS AND SETTERS
	 */

	/**
	 * @param path single {@link String}
	 * @param dependencyPaths an array of {@link String}
	 * @return a single array that is the union of the parameters
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
	@Override
	public List<File> getJavaFiles() {
		return javaFiles;
	}

	/**
	 * @param javaFiles the javaFiles to set
	 */
	@Override
	public void setJavaFiles(List<File> javaFiles) {
		this.javaFiles = javaFiles;
	}

	/**
	 * @return the compilationUnits
	 */
	@Override
	public List<CompilationUnit> getCompilationUnits() {
		return compilationUnits;
	}

	/**
	 * @param compilationUnits the compilationUnits to set
	 */
	@Override
	public void setCompilationUnits(List<CompilationUnit> compilationUnits) {
		this.compilationUnits = compilationUnits;
	}

	/**
	 * @return the packages
	 */
	@Override
	public List<JavaPackage> getPackages() {
		return packages;
	}

	/**
	 * @param packages the packages to set
	 */
	@Override
	public void setPackages(List<JavaPackage> packages) {
		this.packages = packages;
	}

	/**
	 * @return the jarLoader
	 */
	@Override
	public JarLoader getJarLoader() {
		return jarLoader;
	}

	/**
	 * @param jarLoader the jarLoader to set
	 */
	@Override
	public void setJarLoader(JarLoader jarLoader) {
		this.jarLoader = jarLoader;
	}

	/*
	 * METHODS
	 */

	/**
	 * Loads the Java files contained into a directory
	 * @param path the path that identifies the directory
	 * @throws IOException if an I/O error occurs when opening the directory
	 */
	@Override
	public void loadJavaFiles(String path) throws IOException {
		this.javaFiles = Explorer.getFiles(path, JAVA_EXTENSION);
	}

	/**
	 * This methods builds the hierarchy from the classes that have been found into the source code.
	 */
	@Override
	public void build() {
		if(!this.javaFiles.isEmpty())
		{
			// parsing Java files
			this.javaFiles.forEach(f -> {
				try {
					this.compilationUnits.add(JavaParser.parse(f));
				} catch (FileNotFoundException e) {
					logger.error("A problem occured while parsing a Java file : " + f.getAbsolutePath(),e);
				}
				logger.debug(f.getAbsolutePath() + " has been successfuly parsed.");
			});
			
			// building type hierarchy
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
	 * @param type JavaType to get imported {@link JavaType}s from
	 * @return a {@link List} of {@link JavaType}s that are imported by <code>type</code>
	 */
	private List<JavaType> getImportedJavaTypes(JavaType type) {
		List<JavaType> result = new ArrayList<>();
		for(ImportDeclaration imp : type.getCompilationUnit().getImports() ) {
			if(imp.isAsterisk()) // import a.b.*
			{
				for(JavaPackage pack : packages) // all packages of the asterisk import
				{
					if(pack.getName().equals(imp.getNameAsString()))
						result.addAll(pack.getJavaTypes());
				}
			} else { // import a.b.C
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
			// getting package name
			Optional<PackageDeclaration> packageDeclaration = cu.getPackageDeclaration();
			String packageName = (packageDeclaration.isPresent())?packageDeclaration.get().getNameAsString():"";
			
			// getting JavaPackage from package name
			JavaPackage jPackage = this.getPackage(packageName);
			
			// getting types from type declarations
			NodeList<TypeDeclaration<?>> types = cu.getTypes();
			for(TypeDeclaration<?> type : types)
			{
				JavaType javaType = null;
				String tempTypeName = type.getNameAsString();
				if(type.isClassOrInterfaceDeclaration()) // the type declaration is a class or an interface declaration
				{
					// constructing the JavaType object
					ClassOrInterfaceDeclaration coid = type.asClassOrInterfaceDeclaration();
					if(coid.getTypeParameters().isEmpty()) // the declared class or interface is not parameterized
						javaType = new JavaType(tempTypeName, jPackage, coid, cu);
					else javaType = new ParameterizedJavaType(tempTypeName, jPackage, coid, cu);
					
					// finding nested types
					for(BodyDeclaration<?> m : coid.getMembers())
					{
						if(m.isTypeDeclaration())
						{
							JavaNestedType javaNestedType = new JavaNestedType(m.asTypeDeclaration().getNameAsString(), jPackage, m.asTypeDeclaration(), cu, javaType);
							javaType.addNestedType(javaNestedType);
						}
					}
				} else {
					new JavaType(tempTypeName, jPackage, type, cu);
				}
			}
		}
	}

	/**
	 * This method returns the JavaPackage named {@code packageName} if it exists {@code packages} or a new package with the name {@code packageName} if it does not.
	 * @param packageName the name of the package to return
	 * @return the package corresponding to <code>packageName</code>
	 */
	private JavaPackage getPackage(String packageName) {
		// trying to find and return the package corresponding to packageName
		for(JavaPackage pack : getPackages())
		{
			if(pack.getName().equals(packageName))
				return pack;
		}
		
		// creating and returning a new package
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
				logger.debug(type.getSimpleName() + " --|> " + et);
				JavaType coi = findClassOrInterface(type, et);
				if(coi != null)
					result.add(coi);
				else logger.warn("Missing dependency : " + et.asString());
			}
		}
		return result;
	}

	/**
	 * This method returns the list of {@link JavaType} that correspond to implemented types of {@code type}.
	 * @param type {@link JavaType} which is investigated.
	 * @return the list of {@link JavaType} that correspond to implemented types of {@code type}.
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
				if((!(type instanceof CompiledJavaType)) && (type instanceof JavaType)) {
					for(FieldDeclaration fd : type.getTypeDeclaration().getFields())
					{
						exploreRelations(pack, type, fd);
					}
				}
			}
		}
	}

	/**
	 * Method that investigates {@link Relation}s between {@link JavaType}s
	 * @param pack package that is investigated
	 * @param type {@link JavaType} from which {@link Relation}s are extracted
	 * @param fd the {@link FieldDeclaration} that gives the {@link Relation}
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
	 * Computes the parameter types.
	 * @param nodeList the elements which are investigated
	 * @return the {@link List} of {@link Type} of elements in <code>nodeList</code>
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
	 * This method finds the {@link JavaType} corresponding to {@code coi}.
	 * @param type the current {@link JavaType}
	 * @param coi Class or Interface type to find
	 * @return the corresponding {@link JavaType}
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

	/**
	 * This method finds the {@link JavaType} ({@code coid}) corresponding to {@code coi} from external source.
	 * @param type the current {@link JavaType}
	 * @param coi Class or Interface type to find
	 * @return the corresponding {@link JavaType}
	 */
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
	 * This methods finds an imported {@link JavaType} from external sources into default packages.
	 * @param type current {@link JavaType}
	 * @param coi Class or Interface type to find
	 * @return the corresponding {@link JavaType}
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
	 * 
	 * @param index of the {@link JavaType} to return
	 * @return the {@link JavaType} at the corresponding code<code>index</code>
	 */
	private JavaType getJavaType(int index) {
		Class<?> clazz;
		try {
			clazz = this.jarLoader.loadClass(this.jarLoader.getClassNames().get(index));
			return new CompiledJavaType(clazz.getSimpleName(), this.getPackage(clazz.getPackageName()), null, null, clazz);
		} catch (ClassNotFoundException e) {
			logger.warn("A dependency is probably missing.",e);
			return null;
		}
	}

	/**
	 * This methods finds an imported {@link JavaType} from external sources into imported packages.
	 * @param type current {@link JavaType}
	 * @param coi Class or Interface type to find
	 * @return the corresponding {@link JavaType}
	 */
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

	/**
	 * @return the path
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}