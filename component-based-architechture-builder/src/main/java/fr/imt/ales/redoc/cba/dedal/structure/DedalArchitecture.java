package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import dedal.ClassConnection;
import dedal.CompClass;
import dedal.CompInstance;
import dedal.DedalDiagram;
import dedal.DedalFactory;
import dedal.InstConnection;
import dedal.Interface;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalArchitecture {

	List<DedalComponentType> types;
	List<DedalComponentInstance> assembly;
	List<DedalComponentClass> configuration;
	List<DedalComponentRole> specification;
	private List<DedalInterfaceType> interfaceTypes;
	private String projectPath;
	private DedalDiagram dedalDiagram;

	public DedalArchitecture(String projectPath, DedalDiagram dedalDiagram) {
		this.types = new ArrayList<>();
		this.assembly = new ArrayList<>();
		this.configuration = new ArrayList<>();
		this.specification = new ArrayList<>();
		this.interfaceTypes = new ArrayList<>();
		this.projectPath = projectPath;
		this.dedalDiagram = dedalDiagram;
	}

	/**
	 * @return the projectPath
	 */
	public String getProjectPath() {
		return projectPath;
	}

	/**
	 * @return the dedalDiagram
	 */
	public DedalDiagram getDedalDiagram() {
		return dedalDiagram;
	}

	public List<DedalComponentType> getTypes() {
		return types;
	}

	public void setTypes(List<DedalComponentType> types) {
		this.types = types;
	}

	public List<DedalComponentInstance> getAssembly() {
		return assembly;
	}

	public void setAssembly(List<DedalComponentInstance> assembly) {
		this.assembly = assembly;
	}

	public List<DedalComponentClass> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(List<DedalComponentClass> configuration) {
		this.configuration = configuration;
	}

	public List<DedalComponentRole> getSpecification() {
		return specification;
	}

	public void setSpecification(List<DedalComponentRole> specification) {
		this.specification = specification;
	}

	/**
	 * @return the interfaceTypes
	 */
	public List<DedalInterfaceType> getInterfaceTypes() {
		return interfaceTypes;
	}

	/**
	 * @param interfaceTypes the interfaceTypes to set
	 */
	public void setInterfaceTypes(List<DedalInterfaceType> interfaceTypes) {
		this.interfaceTypes = interfaceTypes;
	}

	public DedalInterfaceType getInterfaceTypeByJavaType(JavaType jt) {
		for(DedalInterfaceType inter : this.interfaceTypes) {
			if(inter.getjType().equals(jt))
				return inter;
		}
		return null;
	}

	/**
	 * 
	 * @param cc
	 * @return
	 */
	public Boolean compClassExists(CompClass cc) {
		for(DedalComponentClass compClass : this.configuration) {
			if(compClass.getComponentClass().equals(cc)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * 
	 * @param compInstance
	 * @return
	 */
	public Boolean compInstExists(CompInstance ci) {
		for(DedalComponentInstance compInst : this.assembly) {
			if(compInst.getComponentInstance().equals(ci)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public DedalComponentInstance createCompInstIfNotExists(CompInstance ci, DedalFactory factory,
			EList<InstConnection> assemblyConnections) throws IOException {
		if(!this.compInstExists(ci)) {
			DedalComponentInstance compinst = new DedalComponentInstance(this.projectPath, ci, factory, assemblyConnections, this);
			this.assembly.add(compinst);
			return compinst;

		} else {
			return this.findCompInstance(ci);
		}
	}

	public DedalComponentInstance findCompInstance(CompInstance ci) {
		for(DedalComponentInstance compInst : this.assembly) {
			if(compInst.getComponentInstance().equals(ci)) {
				return compInst;
			}
		}
		return null;
	}

	public DedalComponentClass createCompClassIfNotExists(CompClass cc, DedalFactory factory,
			DedalComponentInstance compInstance) throws IOException {
		if(!this.compClassExists(cc)) {
			DedalComponentClass compClass = new DedalComponentClass(this.projectPath, cc, factory, this, compInstance);
			this.configuration.add(compClass);
			return compClass;
		} else {
			DedalComponentClass compClass = this.findCompClass(cc);
			compClass.setInstantiatedBy(compInstance);
			return compClass;
		}	
	}

	public DedalComponentClass findCompClass(CompClass cc) {
		for(DedalComponentClass compClass : this.configuration) {
			if(compClass.getComponentClass().equals(cc)) {
				return compClass;
			}
		}
		return null;
	}

	public void setConfigConnectionFromAsmConnection(ClassConnection ccon, InstConnection acon) {
		if(acon.getClientIntElem() instanceof Interface) {
			ccon.setClientIntElem(((Interface)acon.getClientIntElem()).getInstantiates());
		}
		if(acon.getServerIntElem() instanceof Interface) {
			ccon.setServerIntElem(((Interface)acon.getServerIntElem()).getInstantiates());
		}
	}


}
