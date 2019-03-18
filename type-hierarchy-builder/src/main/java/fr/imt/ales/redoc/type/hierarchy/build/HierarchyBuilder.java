package fr.imt.ales.redoc.type.hierarchy.build;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

import fr.imt.ales.redoc.jarloader.JarLoader;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaPackage;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public interface HierarchyBuilder {

	void build();

	List<CompilationUnit> getCompilationUnits();

	JarLoader getJarLoader();

	List<File> getJavaFiles();

	List<JavaPackage> getPackages();

	String getPath();

	void loadJavaFiles(String path) throws IOException;

	void setCompilationUnits(List<CompilationUnit> compilationUnits);

	void setJarLoader(JarLoader jarLoader);

	void setJavaFiles(List<File> javaFiles);

	void setPackages(List<JavaPackage> packages);

	JavaType findJavaType(String name);

	JavaType createNewCompiledJavaType(Class<?> class1);

	JavaType findJavaType(Class<?> inter);

	
}
