package org.kungfu.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.kungfu.core.Constants;

import com.jfinal.kit.StrKit;

/**
 * Model 生成器
 */
public class ModelGenerator {
	protected String packageTemplate =
			"package %s.%s;%n%n";
	protected String importTemplate =
			"import %s.%s.%s;%n%n";
	protected String classDefineTemplate =
		"/**%n" +
		" * Model, Generated by Robot on " + Constants.DATE_TIME + ".%n" +
		" */%n" +
		"@SuppressWarnings(\"serial\")%n" +
		"public class %s extends %s<%s> {%n";
	protected String daoTemplate =
			"\tpublic static final %s dao = new %s();%n";
	
	protected String modelPackageName;
	protected String baseModelPackageName;
	protected String modelOutputDir;
	protected boolean generateDaoInModel = true;
	
	public ModelGenerator(String modelPackageName, String baseModelPackageName, String modelOutputDir) {
		if (StrKit.isBlank(modelPackageName))
			throw new IllegalArgumentException("modelPackageName can not be blank.");
		if (modelPackageName.contains("/") || modelPackageName.contains("\\"))
			throw new IllegalArgumentException("modelPackageName error : " + modelPackageName);
		if (StrKit.isBlank(baseModelPackageName))
			throw new IllegalArgumentException("baseModelPackageName can not be blank.");
		if (baseModelPackageName.contains("/") || baseModelPackageName.contains("\\"))
			throw new IllegalArgumentException("baseModelPackageName error : " + baseModelPackageName);
		if (StrKit.isBlank(modelOutputDir))
			throw new IllegalArgumentException("modelOutputDir can not be blank.");
		
		this.modelPackageName = modelPackageName;
		this.baseModelPackageName = baseModelPackageName;
		this.modelOutputDir = modelOutputDir;
	}
	
	public void setGenerateDaoInModel(boolean generateDaoInModel) {
		this.generateDaoInModel = generateDaoInModel;
	}
	
	public void generate(List<TableMeta> tableMetas) {
		System.out.println("Generate model ...");
		for (TableMeta tableMeta : tableMetas)
			genModelContent(tableMeta);
		wirtToFile(tableMetas);
	}
	
	protected void genModelContent(TableMeta tableMeta) {
		StringBuilder ret = new StringBuilder();
		genPackage(tableMeta, ret);
		genImport(tableMeta, ret);
		genClassDefine(tableMeta, ret);
		genDao(tableMeta, ret);
		ret.append(String.format("}%n"));
		tableMeta.modelContent = ret.toString();
	}
	
	protected void genPackage(TableMeta tableMeta, StringBuilder ret) {
		ret.append(String.format(packageTemplate, modelPackageName, tableMeta.name.toLowerCase().replaceAll("_", "")));
	}
	
	protected void genImport(TableMeta tableMeta, StringBuilder ret) {
		ret.append(String.format(importTemplate, baseModelPackageName, tableMeta.name.toLowerCase().replaceAll("_", ""), tableMeta.baseModelName));
	}
	
	protected void genClassDefine(TableMeta tableMeta, StringBuilder ret) {
		ret.append(String.format(classDefineTemplate, tableMeta.modelName, tableMeta.baseModelName, tableMeta.modelName));
	}
	
	protected void genDao(TableMeta tableMeta, StringBuilder ret) {
		if (generateDaoInModel)
			ret.append(String.format(daoTemplate, tableMeta.modelName, tableMeta.modelName));
		else
			ret.append(String.format("\t%n"));
	}
	
	protected void wirtToFile(List<TableMeta> tableMetas) {
		try {
			for (TableMeta tableMeta : tableMetas)
				wirtToFile(tableMeta);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 若 model 文件存在，则不生成，以免覆盖用户手写的代码
	 */
	protected void wirtToFile(TableMeta tableMeta) throws IOException {
		File dir = new File(modelOutputDir + File.separator + tableMeta.name.toLowerCase().replaceAll("_", "") );
		if (!dir.exists())
			dir.mkdirs();
		
		String target = modelOutputDir + File.separator + tableMeta.name.toLowerCase().replaceAll("_", "") + File.separator + tableMeta.modelName + ".java";
		
		File file = new File(target);
		if (file.exists()) {
			return ;	// 若 Model 存在，不覆盖
		}
		
		FileWriter fw = new FileWriter(file);
		try {
			fw.write(tableMeta.modelContent);
		}
		finally {
			fw.close();
		}
	}
}

