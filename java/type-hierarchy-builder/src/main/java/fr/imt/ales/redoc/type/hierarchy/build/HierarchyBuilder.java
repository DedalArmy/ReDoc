/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.type.hierarchy.build;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.TypeVariable;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

import fr.imt.ales.redoc.jarloader.JarLoader;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaPackage;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public interface HierarchyBuilder extends Serializable {

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

	JavaType createNewCompiledJavaType(Class<?> class1) throws IOException;

	JavaType findJavaType(Class<?> inter) throws IOException;

	JavaType findJavaType(TypeVariable<?> paramType) throws IOException;

	
}
