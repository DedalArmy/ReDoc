package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.List;

import dedal.CompInstance;
import dedal.Component;
import dedal.DIRECTION;
import dedal.DedalFactory;
import dedal.InstConnection;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaField;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentInstance extends DedalComponentType {

	private CompInstance componentInstance;
	
	public DedalComponentInstance(String projectPath, CompInstance compInstance, DedalFactory factory, List<InstConnection> connections, DedalArchitecture architecture) throws IOException {
		super(projectPath, compInstance, factory, architecture);
		for( DedalInterface inter : this.interfaces) {
			this.componentInstance.getCompInterfaces().add(inter.getCompInterface());
		}
		this.mapInterfaces();
		this.mapRequiredInterfaces(connections);
		for(DedalInterface inter : this.interfaces) {
			this.componentInstance.getCompInterfaces().add(inter.getCompInterface());
		}
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
	
	/**
	 * @throws IOException
	 */
	protected void mapInterfaces() throws IOException {
		this.interfaces.add(new DedalInterface(this.getProjectPath(), this.getDedalFactory(), this.getjType(), this.architecture));
	}

	/**
	 * Caution, this method has side effects on connections
	 * @param connections
	 * @throws IOException
	 */
	protected void mapRequiredInterfaces(List<InstConnection> connections) throws IOException {
		for(InstConnection connection : connections) {
			String name = connection.getProperty().substring(connection.getProperty().lastIndexOf('.') + 1);
			JavaField jField = this.getjType().getRequiredType(name);
			
			if(jField != null && this.componentInstance.equals(connection.getClientInstElem())) {
				JavaType jt = this.hierarchyBuilder.findJavaType(jField.getType());
				DedalInterface inter = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), jt, this.architecture);
				inter.getCompInterface().setDirection(DIRECTION.REQUIRED);
				this.interfaces.add(inter);
				connection.setClientIntElem(inter.getCompInterface());
			} else if(this.componentInstance.equals(connection.getServerInstElem())) {
				connection.setServerIntElem(this.interfaces.get(0).getCompInterface());
			}
		}
	}

	/**
	 * @return the componentInstance
	 */
	public CompInstance getComponentInstance() {
		return componentInstance;
	}
}
