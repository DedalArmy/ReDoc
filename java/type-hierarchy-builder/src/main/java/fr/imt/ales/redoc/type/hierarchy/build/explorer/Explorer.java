/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.type.hierarchy.build.explorer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.imt.ales.redoc.folderloader.FolderLoader;

/**
 * A class for exploring folders and getting {@link File}s from them.
 * @author Alexandre Le Borgne
 *
 */
public class Explorer {
	/*
	 * LOGGER
	 */
	/**
	 * The {@link Logger} of the class.
	 */
	static final Logger logger = LogManager.getLogger(Explorer.class);

	/**
	 * Private constructor for avoiding instantiation.
	 */
	private Explorer() {}

	/**
	 * This method explores the file system and identifies files with a certain extension.
	 * @param path The URI of the directory to explore.
	 * @param ext The extension to consider.
	 * @return The list of files corresponding to the good extension.
	 * @throws IOException if an I/O error occurs when opening the directory
	 */
	public static List<File> getFiles(String path, String ext) throws IOException {		
		return getFiles(Paths.get(path).toFile().toURI(), ext);
	}

	/**
	 * This method explores the file system and identifies files with a certain extension.
	 * @param uri The URI of the file to explore.
	 * @param ext The extension to consider.
	 * @return The list of files corresponding to the good extension.
	 * @throws IOException if an I/O error occurs when opening the directory
	 */
	private static List<File> getFiles(URI uri, String ext) throws IOException {
		List<File> result = new ArrayList<>();
		List<URI> uris = FolderLoader.recursivelyLoadFolder(Paths.get(uri), ext);
		for(URI uri2 : uris)
		{
			result.add(new File(uri2));
		}
		return result;
	}


}
