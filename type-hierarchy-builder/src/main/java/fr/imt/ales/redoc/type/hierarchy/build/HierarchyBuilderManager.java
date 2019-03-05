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
		return new HierarchyBuilderImpl(path, dependencyPaths);
	}
}
