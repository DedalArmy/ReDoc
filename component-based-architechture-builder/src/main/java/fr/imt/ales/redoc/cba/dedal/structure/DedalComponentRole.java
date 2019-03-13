package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;

import dedal.CompRole;
import dedal.Component;
import dedal.DedalFactory;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentRole extends DedalComponentType {

	CompRole componentRole;
	
	public DedalComponentRole(String projectPath, Component component, DedalFactory dedalFactory, DedalArchitecture architecture) throws IOException {
		super(projectPath, component, dedalFactory, architecture);
	}

	@Override
	protected void initJType(Component component) {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	protected void mapInterfaces() {
//		// TODO Auto-generated method stub
//		
//	}

}
