package fr.imt.ales.redoc.type.hierarchy.structure.serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaPackage;

public class BuilderSerializer {
	public static void serializePackages(HierarchyBuilder builder, String path) throws IOException {
		File f = new File(path);
		f.getParentFile().mkdirs();
		FileOutputStream fout = new FileOutputStream(path);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(builder.getPackages());
		oos.close();
		fout.close();
	}
	
	public static List<JavaPackage> deserializePackages(String path) throws IOException, ClassNotFoundException {
		FileInputStream fin = new FileInputStream(path); 
        ObjectInputStream ois = new ObjectInputStream(fin);
        List<JavaPackage> packages = null;
        packages = (List<JavaPackage>) ois.readObject();
        ois.close();
        fin.close();
        return packages;
	}
}
