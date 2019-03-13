package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;

import dedal.CompClass;
import dedal.Component;
import dedal.Configuration;
import dedal.DIRECTION;
import dedal.DedalFactory;

public class DedalComponentClass extends DedalComponentType {

	private CompClass componentClass;
	
	public DedalComponentClass(String projectPath, CompClass componentClass, DedalFactory dedalFactory, DedalArchitecture architecture) throws IOException {
		super(projectPath, componentClass, dedalFactory, architecture);
		this.architecture.getConfiguration().add(this);
	}
	
	public DedalComponentClass(String projectPath, CompClass componentClass, DedalFactory dedalFactory, DedalArchitecture architecture, DedalComponentInstance componentInstance) throws IOException {
		this(projectPath, componentClass, dedalFactory, architecture);
		this.mapInterfaces(componentInstance);
		this.componentClass.setImplements(this.getComponentType());
		((Configuration)this.componentClass.eContainer()).getComptypes().add(componentType);
		for(DedalInterface inter : this.interfaces) {
			this.componentClass.getCompInterfaces().add(inter.getCompInterface());
		}
	}

	/**
	 * @return the componentClass
	 */
	public CompClass getComponentClass() {
		return componentClass;
	}

	protected void mapInterfaces(DedalComponentInstance componentInstance) throws IOException {
		for(DedalInterface compInt : componentInstance.getInterfaces()) {
			DedalInterface inter = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), compInt.getInterfaceType().getjType(), this.architecture);
			if(compInt.getCompInterface().getDirection().equals(DIRECTION.REQUIRED)) {
				inter.getCompInterface().setDirection(DIRECTION.REQUIRED);
			}
			compInt.getCompInterface().setInstantiates(inter.getCompInterface());
			this.interfaces.add(inter);
		}
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

	public void setInstantiatedBy(DedalComponentInstance componentInstance) {
		for(DedalInterface compInt : componentInstance.getInterfaces()) {
			for(DedalInterface inter : this.interfaces) {
				if(compInt.getCompInterface().getInstantiates() == null && 
						compInt.getCompInterface().getType().equals(inter.getCompInterface().getType())) {
					compInt.getCompInterface().setInstantiates(inter.getCompInterface());
					break;
				}
			}
		}
	}
}
