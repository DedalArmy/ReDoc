/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.util.EcoreUtil;

import dedal.DedalFactory;
import dedal.Interface;
import dedal.InterfaceType;
import dedal.Parameter;
import dedal.Signature;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaMethod;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaParameter;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

/**
 * @author Alexandre Le Borgne
 *
 */
public class ComponentInterfaceExtractor extends ArtefactExtractor {

	static final Logger logger = LogManager.getLogger(ComponentInterfaceExtractor.class);
	private Map<Interface, List<Interface>> candidateInterfaces;

	/**
	 * @param jType
	 * @param dedalFactory
	 */
	public ComponentInterfaceExtractor(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
		this.setCandidateInterfaces(new HashMap<>());
	}

	public Map<Interface, List<Interface>> getCandidateInterfaces() {
		return candidateInterfaces;
	}

	public void setCandidateInterfaces(Map<Interface, List<Interface>> candidateInterfaces) {
		this.candidateInterfaces = candidateInterfaces;
	}

	/**
	 * @param jType
	 * @return
	 * @throws IOException
	 */
	public Interface mapAsInterface(JavaType jType) throws IOException {
		List<JavaMethod> methods = new ArrayList<>();
		methods.addAll(recursivelyGetMethods(jType));
		if(!methods.isEmpty())
		{
			Interface derivedInterface = this.dedalFactory.createInterface();
			derivedInterface.setName(EcoreUtil.generateUUID().replaceAll("-", ""));
			derivedInterface.setType(this.mapInterfaceType(jType));
			return derivedInterface;
		}
		return null;
	}

	/**
	 * @param jType
	 * @return
	 * @throws IOException
	 */
	public Interface mapAsInterface(JavaType jType, InterfaceType interfaceType) throws IOException {
		List<JavaMethod> methods = new ArrayList<>();
		methods.addAll(recursivelyGetMethods(jType));
		Interface derivedInterface = this.dedalFactory.createInterface();
			derivedInterface.setName(EcoreUtil.generateUUID().replaceAll("-", ""));
			derivedInterface.setType(interfaceType);
		return derivedInterface;
	}

	/**
	 * @param objectToInspect
	 * @return
	 * @throws IOException
	 */
	private Collection<? extends JavaMethod> recursivelyGetMethods(JavaType objectToInspect) throws IOException {
		List<JavaMethod> methods = new ArrayList<>();
		if(objectToInspect == null) {
			return methods;
		}
		JavaType superclass = objectToInspect.getSuperclass();
		if(superclass!=null && !(Object.class.getName()).equals(superclass.getFullName()))
		{
			methods.addAll(recursivelyGetMethods(superclass));
		}
		for(JavaType i : objectToInspect.getInterfaces())
		{
			methods.addAll(recursivelyGetMethods(i));
		}
		for (JavaMethod m : objectToInspect.getDeclaredMethods())
		{
			if(!methods.contains(m))
			{
				List<JavaMethod> toRemove = new ArrayList<>();
				methods.add(m);
				for(JavaMethod m2 : methods)
				{
					String m1s = m.getName();
					String m2s = m2.getName();
					if(m1s.equals(m2s) && m!=m2)
						toRemove.add(m);
				}
				methods.removeAll(toRemove);
			}

		}		
		return methods;
	}

	public InterfaceType mapInterfaceType(JavaType jType) throws IOException {
		List<JavaMethod> methods = new ArrayList<>();
		methods.addAll(recursivelyGetMethods(jType));
		InterfaceType result = this.dedalFactory.createInterfaceType();
		try {
			result.setName("I" + jType.getSimpleName());
		}
		catch (NullPointerException e) {
			return result;
		}
		if(!methods.isEmpty())
		{
			result.getSignatures().addAll(this.getSignatures(methods));
		}
		return result ;
	}

	/**
	 * 
	 * @param methods
	 * @return
	 */
	private List<Signature> getSignatures(List<JavaMethod> methods)
	{
		List<Signature> result = new ArrayList<>();
		for (JavaMethod m : methods)
		{
			Signature tempSignature = this.dedalFactory.createSignature();
			tempSignature.setName(m.getName());
			tempSignature.setType(m.getReturnType());
			for (int i = 0; i < m.getParameters().size(); i++) {
				Parameter tempParameter = this.dedalFactory.createParameter();
				JavaParameter p = m.getParameters().get(i);
				tempParameter.setName(p.getName());
				tempParameter.setType(p.getType());
				tempSignature.getParameters().add(tempParameter);
			}
			result.add(tempSignature);
		}
		return result;
	}

}
