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
	List<DedalInterfaceType> candidateInterfaceTypes;
	private ComponentInterfaceExtractor cie;

	public DedalInterfaceType(String projectPath, DedalFactory dedalFactory, JavaType jType, ComponentInterfaceExtractor cie, DedalArchitecture architecture) throws IOException {
		super(projectPath, dedalFactory, architecture);
		this.setjType(jType);
		this.candidateInterfaceTypes = new ArrayList<>();
		this.cie = cie;
		this.mapInterfaceType();
		this.architecture.getInterfaceTypes().add(this);
	}

	public void mapInterfaceType() throws IOException {
		if(this.cie == null) {
			this.cie = new ComponentInterfaceExtractor(this.getjType(), this.getDedalFactory());
		}
		this.interfaceType = this.cie.mapInterfaceType(this.getjType());
		List<JavaType> jTypes = this.recursivelyGetSuperTypes(this.getjType());
		for(JavaType jt : jTypes) {
			DedalInterfaceType inter = this.architecture.getInterfaceTypeByJavaType(jt);
			if(inter == null) {
				this.candidateInterfaceTypes.add(new DedalInterfaceType(this.getProjectPath(), this.getDedalFactory(), jt, this.cie, this.architecture));
			}
			else 
				this.candidateInterfaceTypes.add(inter);
		}
	}

	private List<JavaType> recursivelyGetSuperTypes(JavaType jType) {
		List<JavaType> result = new ArrayList<>();
		if(!jType.getjExtends().isEmpty()) {
			result.addAll(jType.getjExtends());
			for(JavaType jt : jType.getjExtends()) {
				result.addAll(this.recursivelyGetSuperTypes(jt));
			}
		}
		if(!jType.getjImplements().isEmpty()) {
			result.addAll(jType.getjImplements());
			for(JavaType jt : jType.getjImplements()) {
				result.addAll(this.recursivelyGetSuperTypes(jt));
			}
		}
		return result ;
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

}
