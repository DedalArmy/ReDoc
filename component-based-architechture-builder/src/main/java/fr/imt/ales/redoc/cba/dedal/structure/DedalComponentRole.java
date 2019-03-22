package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dedal.ClassConnection;
import dedal.CompClass;
import dedal.CompRole;
import dedal.Component;
import dedal.DIRECTION;
import dedal.DedalFactory;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentRoleExtractor;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaField;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentRole extends DedalComponentType {

	CompRole componentRole;
	List<DedalComponentRole> candidateComponentRoles;
	
	public DedalComponentRole(String projectPath, Component component, DedalFactory dedalFactory, 
			DedalArchitecture architecture, Component sourceComponent, 
			List<ClassConnection> configConnections, CompClass initialComponentClass) throws IOException {
		super(projectPath, component, dedalFactory, architecture);
		this.candidateComponentRoles = this.computeCandidateComponentRoles(sourceComponent, configConnections, initialComponentClass);
		this.mapInterfaces(sourceComponent, configConnections, initialComponentClass);
	}

	private void mapInterfaces(Component sourceComponent, List<ClassConnection> configConnections, CompClass initialComponentClass) throws IOException {
		if(sourceComponent.equals(initialComponentClass)) { // this is the first role that is extracted which corresponds to the bigger role
			DedalComponentClass dcc = this.architecture.getConfigComponent((CompClass)sourceComponent);
			for(DedalInterface inter : dcc.getInterfaces()) {
				DedalInterface dedalInterface = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), 
						inter.getInterfaceType().getjType(), this.architecture);
				dedalInterface.getCompInterface().setDirection(inter.getCompInterface().getDirection());
				this.interfaces.add(dedalInterface);
				this.componentRole.getCompInterfaces().add(dedalInterface.getCompInterface());
			}
		} else { // the case of a candidate component role
			this.interfaces.add(new DedalInterface(this.getProjectPath(), this.getDedalFactory(), this.getjType(), this.architecture));
			this.mapRequiredInterfaces(configConnections, initialComponentClass);
			for(DedalInterface inter : this.interfaces) {
				this.componentRole.getCompInterfaces().add(inter.getCompInterface());
			}
		}
	}

	private void mapRequiredInterfaces(List<ClassConnection> configConnections, CompClass initialComponentClass) throws IOException {
		List<ClassConnection> clientConnections = new ArrayList<>(); // when initialComponentClass is the client in a connection
		for(ClassConnection con : configConnections) {
			if(con.getClientClassElem().equals(initialComponentClass)) {
				clientConnections.add(con);
			}
		}
		for(ClassConnection ccon : clientConnections) {
			String name = ccon.getProperty().substring(ccon.getProperty().lastIndexOf('.') + 1);
			JavaField jField = this.getjType().getRequiredType(name);
			if(jField != null && initialComponentClass.equals(ccon.getClientClassElem())) {
				JavaType jt = this.hierarchyBuilder.findJavaType(jField.getType());
				DedalInterface inter = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), jt, this.architecture);
				inter.getCompInterface().setDirection(DIRECTION.REQUIRED);
				this.interfaces.add(inter);
				ccon.setClientIntElem(inter.getCompInterface());
			} else if(initialComponentClass.equals(ccon.getServerClassElem())) {
				ccon.setServerIntElem(this.interfaces.get(0).getCompInterface()); // because the first interface is the global provided one
			}
		}
	}

	private List<DedalComponentRole> computeCandidateComponentRoles(Component sourceComponent, 
			List<ClassConnection> configConnections, CompClass initialComponentClass) throws IOException {
		List<DedalComponentRole> result = new ArrayList<>();
		List<JavaType> jTypes = this.getSuperTypes(this.getjType());
		for(JavaType jt : jTypes) {
			if(jt!=null && !jt.equals(this.getjType())) {
				DedalComponentRole dcr = this.architecture.getComponentRoleByJavaType(jt);
				if(dcr != null) {
					result.add(dcr);
				}
				else {
					ComponentRoleExtractor cre = new ComponentRoleExtractor(jt, this.getDedalFactory());
					CompRole component = cre.mapComponentRole();
					result.add(new DedalComponentRole(this.getProjectPath(), component, this.getDedalFactory(), 
							this.architecture, this.componentRole, configConnections, initialComponentClass));
				}
			}
		}
		return result;
	}

	private List<JavaType> getSuperTypes(JavaType jType) {
		List<JavaType> result = new ArrayList<>();
		if(!jType.getjExtends().isEmpty()) {
			result.addAll(jType.getjExtends());
		}
		if(!jType.getjImplements().isEmpty()) {
			result.addAll(jType.getjImplements());
		}
		return result;
	}

	@Override
	protected void initJType(Component component) {
		if(component instanceof CompRole) {
			this.componentRole = (CompRole) component;
			this.setjType(this.hierarchyBuilder.findJavaType(this.componentRole.getName().substring(0, this.componentRole.getName().lastIndexOf('_')).replaceAll("\"", "")));
		}
	}

	public CompRole getComponentRole() {
		return this.componentRole;
	}
	
	@Override
	public String toString() {
		return this.componentRole.getName();
	}

//	@Override
//	protected void mapInterfaces() {
//		// TODO Auto-generated method stub
//		
//	}

}
