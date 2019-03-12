package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dedal.CompType;
import dedal.Component;
import dedal.Connection;
import dedal.DIRECTION;
import dedal.DedalFactory;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentTypeExtractor;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilderManager;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaField;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public abstract class DedalComponentType extends DedalType {

	CompType componentType;
	List<DedalInterface> interfaces;
	
	public DedalComponentType(String projectPath, Component component, DedalFactory dedalFactory) throws IOException {
		super(projectPath, dedalFactory);
		this.componentType = null;
		this.initJType(component);
		this.interfaces = new ArrayList<>();
		this.mapInterfaces();
	}
	
	public CompType getComponentType() {
		if(this.componentType == null)
			this.computeComponentType();
		return this.componentType;
	}

	private void computeComponentType() {
		ComponentTypeExtractor cte = new ComponentTypeExtractor(this.getjType(), this.dedalFactory);
		this.componentType = cte.mapCompType();
	}
	
	protected void mapInterfaces() throws IOException {
		this.interfaces.add(new DedalInterface(this.getProjectPath(), this.getDedalFactory(), this.getjType()));
	}
	
	protected void mapRequiredInterfaces(List<? extends Connection> connections) throws IOException {
		for(Connection connection : connections) {
			String name = connection.getProperty().substring(connection.getProperty().lastIndexOf('.') + 1);
			JavaField jField = this.getjType().getFieldByName(name);
			if(jField != null) {
				HierarchyBuilder hb = HierarchyBuilderManager.getInstance().getHierarchyBuilder(this.getProjectPath());
				JavaType jt = hb.findJavaType(jField.getType());
				DedalInterface inter = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), jt);
				inter.getCompInterface().setDirection(DIRECTION.REQUIRED);
				this.interfaces.add(inter);
			}
		}
	}
	
	protected abstract void initJType(Component component);

}
