package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import dedal.DedalFactory;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilderManager;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public abstract class DedalType {

	private String projectPath;
	protected HierarchyBuilder hierarchyBuilder;
	protected DedalFactory dedalFactory;
	private JavaType jType;

	/**
	 * @param projectPath
	 * @param dedalFactory
	 * @throws IOException 
	 */
	public DedalType(String projectPath, DedalFactory dedalFactory) throws IOException {
		this.projectPath = projectPath;
		this.dedalFactory = dedalFactory;
		hierarchyBuilder = HierarchyBuilderManager.getInstance().getHierarchyBuilder(projectPath);
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public DedalFactory getDedalFactory() {
		return dedalFactory;
	}

	public void setDedalFactory(DedalFactory dedalFactory) {
		this.dedalFactory = dedalFactory;
	}

	/**
	 * @return the jType
	 */
	public JavaType getjType() {
		return jType;
	}

	/**
	 * @param jType the jType to set
	 */
	public void setjType(JavaType jType) {
		this.jType = jType;
	}


	
}
