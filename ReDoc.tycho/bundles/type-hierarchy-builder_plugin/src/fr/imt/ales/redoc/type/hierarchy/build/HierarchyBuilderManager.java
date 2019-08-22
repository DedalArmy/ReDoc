/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.type.hierarchy.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HierarchyBuilderManager {
	
	/**
	 * {@link List} of {@link HierarchyBuilder}s
	 */
	private static List<HierarchyBuilder> hierarchyBuilders = new ArrayList<>();
	/**
	 * Singleton instance
	 */
	private static HierarchyBuilderManager INSTANCE = new HierarchyBuilderManager();
	
	/**
	 * To prevent instantiation
	 */
	private HierarchyBuilderManager() {
	}
	
	/**
	 * To get the instance
	 * @return
	 */
	public static HierarchyBuilderManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Get a specific instance of a {@link HierarchyBuilder} or create it if it does not exists yet.
	 * @param path
	 * @param dependencyPaths
	 * @return
	 * @throws IOException
	 */
	public HierarchyBuilder getHierarchyBuilder(String path, String ... dependencyPaths) throws IOException {
		for(HierarchyBuilder hb : hierarchyBuilders) {
			if(path.equals(hb.getPath()))
				return hb;
		}
		HierarchyBuilderImpl hb = new HierarchyBuilderImpl(path, dependencyPaths);
		hierarchyBuilders.add(hb);
		return hb;
	}

	public void init() {
		HierarchyBuilderManager.hierarchyBuilders = new ArrayList<>();
	}
}
