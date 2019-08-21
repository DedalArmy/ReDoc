package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;

import dedal.DedalFactory;
import dedal.Interface;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentInterfaceExtractor;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalInterface {

	Interface compInterface;
	DedalInterfaceType interfaceType;
	private ComponentInterfaceExtractor cie;
	
	public DedalInterface(String projectPath, DedalFactory dedalFactory, JavaType jType, DedalArchitecture architecture) throws IOException {
		this.cie = new ComponentInterfaceExtractor(jType, dedalFactory);
		this.interfaceType = initInterfaceType(projectPath, dedalFactory, jType, architecture);
		this.compInterface = cie.mapAsInterface(jType, this.interfaceType.getInterfaceType());
	}

	/**
	 * @return the interfaceType
	 */
	public DedalInterfaceType getInterfaceType() {
		return interfaceType;
	}

	private DedalInterfaceType initInterfaceType(String projectPath, DedalFactory dedalFactory, JavaType jType,
			DedalArchitecture architecture) throws IOException {
		DedalInterfaceType result = architecture.getInterfaceTypeByJavaType(jType);
		return result == null? new DedalInterfaceType(projectPath, dedalFactory, jType, this.cie, architecture):result;
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
