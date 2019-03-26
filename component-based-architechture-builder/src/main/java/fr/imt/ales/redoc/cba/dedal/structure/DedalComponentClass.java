package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import dedal.ClassConnection;
import dedal.CompClass;
import dedal.CompRole;
import dedal.Component;
import dedal.Configuration;
import dedal.DIRECTION;
import dedal.DedalFactory;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentRoleExtractor;

public class DedalComponentClass extends DedalComponentType {

	private CompClass componentClass;
	
	public DedalComponentClass(String projectPath, CompClass componentClass, DedalFactory dedalFactory, DedalArchitecture architecture) throws IOException {
		super(projectPath, componentClass, dedalFactory, architecture);
		this.architecture.getConfiguration().add(this);
	}
	
	public DedalComponentClass(String projectPath, CompClass componentClass, DedalFactory dedalFactory, DedalArchitecture architecture, DedalComponentInstance componentInstance) throws IOException {
		this(projectPath, componentClass, dedalFactory, architecture);
		this.mapInterfaces(componentInstance);
		this.renameProvidedInterfaces();
		this.componentClass.setImplements(this.getComponentType());
		((Configuration)this.componentClass.eContainer()).getComptypes().add(componentType);
		for(DedalInterface inter : this.interfaces) {
			this.componentClass.getCompInterfaces().add(inter.getCompInterface());
		}
	}

	private void renameProvidedInterfaces() {
		for(DedalInterface inter : this.interfaces) {
			if(inter.getCompInterface().getDirection().equals(DIRECTION.PROVIDED)) {
				String name = this.componentClass.getName().replaceAll("\"", "")+".prov"+inter.getCompInterface().getType().getName();
				inter.getCompInterface().setName(name);
			}
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
				inter.getCompInterface().setName(this.componentClass.getName().replaceAll("\"", "")
						+compInt.getCompInterface().getName().substring(compInt.getCompInterface().getName().lastIndexOf('.')));
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

	public List<DedalComponentRole> computeComponentRoles(EList<ClassConnection> configConnections) throws IOException {
		List<DedalComponentRole> result = new ArrayList<>();
		ComponentRoleExtractor cre = new ComponentRoleExtractor(this.getjType(), this.getDedalFactory());
		CompRole compRole = cre.mapComponentRole();
		result.add(new DedalComponentRole(this.getProjectPath(), compRole, this.getDedalFactory(), this.architecture, this.componentClass, configConnections, this.componentClass));
		return result ;
	}
}
