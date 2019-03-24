package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dedal.DedalFactory;
import dedal.InterfaceType;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentInterfaceExtractor;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalInterfaceType extends DedalType {

	InterfaceType interfaceType;
	private List<DedalInterfaceType> candidateInterfaceTypes;
	private ComponentInterfaceExtractor cie;

	public DedalInterfaceType(String projectPath, DedalFactory dedalFactory, JavaType jType, ComponentInterfaceExtractor cie, DedalArchitecture architecture) throws IOException {
		super(projectPath, dedalFactory, architecture);
		this.setjType(jType);
		this.setCandidateInterfaceTypes(new ArrayList<>());
		this.cie = cie;
		this.mapInterfaceType();
		this.architecture.getInterfaceTypes().add(this);
	}

	public void mapInterfaceType() throws IOException {
		if(this.cie == null) {
			this.cie = new ComponentInterfaceExtractor(this.getjType(), this.getDedalFactory());
		}
		this.interfaceType = this.cie.mapInterfaceType(this.getjType());
		List<JavaType> jTypes = this.getSuperTypes(this.getjType());
		for(JavaType jt : jTypes) {
			DedalInterfaceType inter = this.architecture.getInterfaceTypeByJavaType(jt);
			if(inter == null) {
				this.getCandidateInterfaceTypes().add(new DedalInterfaceType(this.getProjectPath(), this.getDedalFactory(), jt, this.cie, this.architecture));
			}
			else 
				this.getCandidateInterfaceTypes().add(inter);
		}
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

	/**
	 * @return the interfaceType
	 */
	public InterfaceType getInterfaceType() {
		return interfaceType;
	}

	@Override
	public String toString() {
		return this.interfaceType.getName();
	}

	/**
	 * @return the candidateInterfaceTypes
	 */
	public List<DedalInterfaceType> getCandidateInterfaceTypes() {
		return candidateInterfaceTypes;
	}

	/**
	 * @param candidateInterfaceTypes the candidateInterfaceTypes to set
	 */
	public void setCandidateInterfaceTypes(List<DedalInterfaceType> candidateInterfaceTypes) {
		this.candidateInterfaceTypes = candidateInterfaceTypes;
	}

}
