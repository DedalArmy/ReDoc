package fr.imt.ales.redoc.cba.dedal.structure;

import java.util.ArrayList;
import java.util.List;

public class DedalArchitecture {
	
	List<DedalType> types;
	List<DedalComponentInstance> assembly;
	List<DedalComponentClass> configuration;
	List<DedalComponentRole> specification;
	List<DedalConnection> dedalConnections;
	
	public DedalArchitecture(String projectPath) {
		this.types = new ArrayList<>();
		this.assembly = new ArrayList<>();
		this.configuration = new ArrayList<>();
		this.specification = new ArrayList<>();
		this.dedalConnections = new ArrayList<>();
	}

	public List<DedalType> getTypes() {
		return types;
	}

	public void setTypes(List<DedalType> types) {
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

	public List<DedalConnection> getDedalConnections() {
		return dedalConnections;
	}

	public void setDedalConnections(List<DedalConnection> dedalConnections) {
		this.dedalConnections = dedalConnections;
	}
	
	
}
