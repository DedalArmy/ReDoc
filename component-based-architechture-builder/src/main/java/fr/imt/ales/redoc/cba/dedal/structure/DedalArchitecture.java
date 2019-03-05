package fr.imt.ales.redoc.cba.dedal.structure;

import java.util.List;

public class DedalArchitecture {
	List<DedalType> types;
	List<DedalComponentInstance> assembly;
	List<DedalComponentClass> configuration;
	List<DedalComponentRole> specification;
	List<DedalConnection> dedalConnections;
}
