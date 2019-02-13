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
					if(logger.isErrorEnabled())
					{
						logger.error("A problem occured while parsing a Java file : " + f.getAbsolutePath(),e);
					}
				}
				if(logger.isDebugEnabled())
				{
					logger.debug(f.getAbsolutePath() + " has been successfuly parsed.");
				}
			});
			if(!this.compilationUnits.isEmpty())
			{
				this.exploreHierarchy();
				this.exploreRelations();
			}
			else if(logger.isWarnEnabled())
			{
				logger.warn("The list of Java compilation units is empty and then the hierarchy construction cannot go further.");
			}
		}
		else if(logger.isWarnEnabled())
		{
			logger.warn("The list of Java files is empty.");
		}

	}

	/**
	 * This methods explores and extracts the type hierarchy from the project
	 */
	private void exploreHierarchy() {
		this.loadNecessaryData();
		for(JavaPackage pack : getPackages())
		{
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
							jPackage.addJavaType(javaNestedType);
						}
					}
				} else {
					javaType = new JavaType(typeName, jPackage, type, cu);
				}
				jPackage.addJavaType(javaType);
			}
		}
		if(logger.isTraceEnabled())
		{
			logger.trace("packages \n" + this.getPackages());
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
		this.getPackages().add(pack);
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
				result.add(findClassOrInterface(type, et));
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
				result.add(findClassOrInterface(type, et));
			}
		}
		return result;
	}

	/**
	 * This method explores and extracts relations between types
	 */
	private void exploreRelations() {
		for(JavaPackage pack : getPackages())
		{
			for(JavaType type : pack.getJavaTypes())
			{
				for( FieldDeclaration fd : type.getTypeDeclaration().getFields())
				{
					exploreRelations(pack, type, fd);
				}
			}
		}
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
				else { //The type may be a nested type, or it may have been imported from external source.
					if(logger.isDebugEnabled())
					{
						logger.debug("External dependencies should be investigated, \"" + fd + "\" could not be determined.");
					}
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
		/*
		 * LOCAL DEPENDENCIES
		 */
		for(JavaType imp : type.getjImports()) {
			if(imp.getSimpleName().equals(coi.getNameAsString()) || imp.getFullName().equals(coi.getNameAsString()))
				return imp;
		}
		// If still into the method, the dependency is not into the imports it is maybe into the same package
		JavaType tempJavaType = type.getjPackage().findTypeByName(coi.getNameAsString());
		if(tempJavaType!=null)
		{
			return tempJavaType;
		}
		//If still running, then it could be an explicit type (fr.package.name.ClassName)
		for(JavaPackage pack : this.packages)
		{
			tempJavaType = pack.findTypeByName(coi.getNameAsString());
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
		 * MEME PACKAGE OK PACKAGE.CLASSE
		 * PACKAGE DIFFERENT OK PACKAGE.CLASSE
		 * PACKAGE SANS NOM... PAS OK, A VOIR AVEC DES ESSAIS (JAR SANS PACKAGE + IMPORT --> VOIR AVEC LE CLASSPATH?) SINON ON CONSIDERE QUE PERSONNE FAIT CA!
		 */
//		List<String> truc = this.jarLoader.getPackageNameToClassNames().get("net.sourceforge.plantuml");
		System.out.println();
		
		/*
		 * FIND IN IMPORTS
		 */
		try {
			result = findTypeInImports(type, coi);
			if(result != null)
				return result;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		/*
		 * FIND IN DEFAULT PACKAGE
		 */
		result = findTypeInDefaultPackages(type, coi);
		return (result != null)? result : null;
	}

	private JavaType findTypeInImports(JavaType type, ClassOrInterfaceType coi) throws ClassNotFoundException {
		int index = this.jarLoader.getClassNames().indexOf(coi.asString());
		JavaType result;
		if(index>=0) {
			
			Class<?> clazz = this.jarLoader.loadClass(this.jarLoader.getClassNames().get(index));
			if(clazz != null) {
//				result = new JavaType(clazz.getSimpleName(), jPackage, typeDeclaration, compilationUnit);
			}
		}
		NodeList<ImportDeclaration> imports = type.getCompilationUnit().getImports();
		for(ImportDeclaration imp : imports) {
			if(imp.isAsterisk()) {
				
			} else {
				
			}
		}
		return null;
	}

	private JavaType findTypeInDefaultPackages(JavaType type, ClassOrInterfaceType coi) {
		// TODO Auto-generated method stub
		return null;
	}
}