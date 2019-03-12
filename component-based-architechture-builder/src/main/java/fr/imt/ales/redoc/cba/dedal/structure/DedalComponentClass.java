package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;

import dedal.CompClass;
import dedal.Component;
import dedal.DedalFactory;

public class DedalComponentClass extends DedalComponentType {

	private CompClass componentClass;
	
	public DedalComponentClass(String projectPath, CompClass componentClass, DedalFactory dedalFactory) throws IOException {
		super(projectPath, componentClass, dedalFactory);
	}

	@Override
	protected void initJType(Component component) {
		if(component instanceof CompClass) {
			this.componentClass = (CompClass) component;
			this.setjType(this.hierarchyBuilder.findJavaType(componentClass.getName().replaceAll("\"", "")));
		}
	}

	@Override
	public String toString() {
		return this.componentClass.getName();
	}
}
