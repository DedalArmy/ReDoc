package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import dedal.CompInstance;
import dedal.Component;
import dedal.DIRECTION;
import dedal.DedalFactory;
import dedal.InstConnection;
import dedal.Interaction;
import dedal.Interface;
import fr.imt.ales.redoc.cba.dedal.builder.InterfaceOption;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaField;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentInstance extends DedalComponentType {

	private CompInstance componentInstance;
	private List<InstConnection> connections;
	
	public DedalComponentInstance(String projectPath, CompInstance compInstance, DedalFactory factory, List<InstConnection> connections, DedalArchitecture architecture) throws IOException {
		super(projectPath, compInstance, factory, architecture);
		this.connections = connections;
		this.architecture.getAssembly().add(this);
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
		DedalInterface inter = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), this.getjType(), this.architecture);
		String name = this.componentInstance.getName()+".prov"+inter.getCompInterface().getType().getName();
		inter.getCompInterface().setName(name);
		this.interfaces.add(inter);
	}

	/**
	 * Caution, this method has side effects on connections
	 * @param connections
	 * @throws IOException
	 */
	protected void mapRequiredInterfaces(List<InstConnection> connections) throws IOException {
		List<InstConnection> toRemove = new ArrayList<>();
		for(InstConnection connection : connections) {
			if(connection.getProperty()!=null) {
				String name = connection.getProperty().substring(connection.getProperty().lastIndexOf('.') + 1);
				JavaField jField = this.getjType().getRequiredType(name);
				if(jField != null && this.componentInstance.equals(connection.getClientInstElem())) {
					JavaType jt = this.hierarchyBuilder.findJavaType(jField.getType());
					if(!this.interfaceExists(jt)) {
						DedalInterface inter = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), jt, this.architecture);
						inter.getCompInterface().setName(connection.getProperty());
						inter.getCompInterface().setDirection(DIRECTION.REQUIRED);
						this.interfaces.add(inter);
						connection.setClientIntElem(inter.getCompInterface());
					} else {
						connection.setClientIntElem(this.findCompInterface(jt));
					}
				} else if(this.componentInstance.equals(connection.getServerInstElem())) {
					connection.setServerIntElem(this.interfaces.get(0).getCompInterface());
				}
			} else {
				toRemove.add(connection);
			}
		}
		connections.removeAll(toRemove); // We remove the failed connections to continue the reconstruction
	}

	private Interaction findCompInterface(JavaType jt) {
		for(DedalInterface inter : this.interfaces) {
			if(inter.getInterfaceType().getjType().equals(jt))
				return inter.getCompInterface();
		}
		return null;
	}

	private Boolean interfaceExists(JavaType jt) {
		for(DedalInterface inter : this.interfaces) {
			if(inter.getInterfaceType().getjType().equals(jt))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * @return the componentInstance
	 */
	public CompInstance getComponentInstance() {
		return componentInstance;
	}

	public void refine(InterfaceOption interfaceOption) throws IOException {
		List<DedalInterfaceType> superTypes = new ArrayList<>();
		if(interfaceOption.equals(InterfaceOption.SMALLINTERFACES)) {
			superTypes.addAll(this.gatherSuperInterfaces());
			for(DedalInterfaceType candidate : superTypes) { // Add all interfaces to the compInstance
				DedalInterface inter = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), candidate.getjType(), this.architecture);
				this.interfaces.add(inter);
				this.componentInstance.getCompInterfaces().add(inter.getCompInterface());
			}
			for(InstConnection conn : this.connections) {
				this.setSmallestInterface(conn);
			}
		}
		for(DedalInterface inter : this.interfaces) {
			if(inter.getCompInterface().getDirection().equals(DIRECTION.PROVIDED)) {
				String name = this.componentInstance.getName().replaceAll("\"", "")+".prov"+inter.getCompInterface().getType().getName();
				inter.getCompInterface().setName(name);
			}
		}
		
	}

	private void setSmallestInterface(InstConnection conn) {
		if(conn.getServerInstElem().equals(this.componentInstance)) {
			DedalInterfaceType minInterfaceType = this.architecture.findInterfaceType(((Interface)conn.getClientIntElem()).getType());
			DedalInterfaceType min = this.architecture.findInterfaceType(((Interface)conn.getServerIntElem()).getType());
			if(!min.getjType().equals(minInterfaceType.getjType())) { //else it means that the smallest interface type is already set
				for(DedalInterface inter : this.interfaces) {
					if(!inter.getInterfaceType().getjType().equals(min.getjType()) && min.getjType().isSubtypeOf(inter.getInterfaceType().getjType()) 
							&& inter.getInterfaceType().getjType().isSubtypeOf(minInterfaceType.getjType())) {
						min = inter.getInterfaceType();
						conn.setServerIntElem(inter.getCompInterface());
					}
				}
			}
		}
	}

	private List<DedalInterfaceType> gatherSuperInterfaces() {
		List<DedalInterfaceType> result = new ArrayList<>();
		for(DedalInterface inter : this.interfaces) {
			if(inter.getCompInterface().getDirection().equals(DIRECTION.PROVIDED)) {
				result.addAll(this.gatherSuperInterfaces(inter.getInterfaceType()));
			}
		}
		return result;
	}
	


	private List<DedalInterfaceType> gatherSuperInterfaces(DedalInterfaceType interfaceType) {
		List<DedalInterfaceType> result = new ArrayList<>();
		if(!interfaceType.getCandidateInterfaceTypes().isEmpty()) {
			result.addAll(interfaceType.getCandidateInterfaceTypes());
			for(DedalInterfaceType cand : interfaceType.getCandidateInterfaceTypes()) {
				result.addAll(this.gatherSuperInterfaces(cand));
			}
		}
		return result;
	}
}
