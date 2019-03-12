package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;

import dedal.DedalFactory;
import dedal.Interface;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentInterfaceExtractor;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalInterface {

	Interface compInterface;
	DedalInterfaceType interfaceType;
	private ComponentInterfaceExtractor cie;
	
	public DedalInterface(String projectPath, DedalFactory dedalFactory, JavaType jType) throws IOException {
		this.cie = new ComponentInterfaceExtractor(jType, dedalFactory);
		this.interfaceType = new DedalInterfaceType(projectPath, dedalFactory, jType, this.cie);
		this.compInterface = cie.mapAsInterface(jType, this.interfaceType.getInterfaceType());
	}

	/**
	 * @return the compInterface
	 */
	public Interface getCompInterface() {
		return compInterface;
	}
	
	@Override
	public String toString() {
		return this.compInterface.getName() + " : " + this.interfaceType;
	}
}
