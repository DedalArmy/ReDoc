package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.List;

import dedal.CompInstance;
import dedal.Component;
import dedal.DedalFactory;
import dedal.InstConnection;

public class DedalComponentInstance extends DedalComponentType {

	private CompInstance componentInstance;
	
	public DedalComponentInstance(String projectPath, CompInstance compInstance, DedalFactory factory, List<InstConnection> connections) throws IOException {
		super(projectPath, compInstance, factory);
		for( DedalInterface inter : this.interfaces) {
			this.componentInstance.getCompInterfaces().add(inter.getCompInterface());
		}
		this.mapRequiredInterfaces(connections);
	}

	@Override
	protected void initJType(Component component) {
		if(component instanceof CompInstance) {
			this.componentInstance = (CompInstance) component;
			this.setjType(this.hierarchyBuilder.findJavaType(componentInstance.getInstantiates().getName().replaceAll("\"", "")));
		}
	}
	
	@Override
	public String toString() {
		return this.componentInstance.getName();
	}
	
	
}
