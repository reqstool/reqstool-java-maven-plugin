// Copyright © reqstool
package io.github.reqstool.plugins.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.fasterxml.jackson.databind.JsonNode;

import io.github.reqstool.annotations.SVCs;

class RequirementsToolMojoTests {

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = RequirementsToolMojo.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static RequirementsToolMojo newMojoForExecute(File outputDirectory, MavenProjectHelper projectHelper)
			throws Exception {
		RequirementsToolMojo mojo = new RequirementsToolMojo();

		MavenProject mavenProject = new MavenProject();
		Build build = new Build();
		build.setFinalName("test-project");
		mavenProject.setBuild(build);
		Field basedirField = mavenProject.getClass().getDeclaredField("basedir");
		basedirField.setAccessible(true);
		basedirField.set(mavenProject, outputDirectory);

		setField(mojo, "project", mavenProject);
		setField(mojo, "projectHelper", projectHelper);
		setField(mojo, "outputDirectory", outputDirectory);
		setField(mojo, "requirementsAnnotationsFile",
				new File(outputDirectory, "missing-requirements-annotations.yml"));
		setField(mojo, "svcsAnnotationsFile", new File(outputDirectory, "missing-svcs-annotations.yml"));
		setField(mojo, "testResults", new String[] { "test_results/**/*.xml" });
		return mojo;
	}

	@SVCs("SVC_MAVEN_PLUGIN_001")
	@Test
	void testCombine() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();

		File implementationsAnnotationsFile = new File(
				classLoader.getResource("yml/requirements_annotations.yml").getFile());
		File testsAnnotationsFile = new File(classLoader.getResource("yml/svcs_annotations.yml").getFile());

		JsonNode implementationsNode = RequirementsToolMojo.yamlMapper.readTree(implementationsAnnotationsFile)
			.path(RequirementsToolMojo.XML_REQUIREMENT_ANNOTATIONS)
			.path(RequirementsToolMojo.XML_IMPLEMENTATIONS);

		JsonNode testsNode = RequirementsToolMojo.yamlMapper.readTree(testsAnnotationsFile)
			.path(RequirementsToolMojo.XML_REQUIREMENT_ANNOTATIONS)
			.path(RequirementsToolMojo.XML_TESTS);

		JsonNode combinedNode = RequirementsToolMojo.combineOutput(implementationsNode, testsNode);

		String combinedResult = RequirementsToolMojo.yamlMapper.writeValueAsString(combinedNode);

		File combinedAnnotationsFile = new File(classLoader.getResource("yml/combined_annotations.yml").getFile());

		byte[] combinedFileContent = Files.readAllBytes(combinedAnnotationsFile.toPath());
		String combinedFileContentAsString = new String(combinedFileContent);

		assertEquals(combinedFileContentAsString, combinedResult, "The combined annotations files should match");
	}

	@SVCs("SVC_MAVEN_PLUGIN_002")
	@Test
	void testAssembleZipArtifact() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		URL resourcePath = classLoader.getResource("zip");
		Path zipResourcePath = Paths.get(resourcePath.getFile());

		RequirementsToolMojo mojo = new RequirementsToolMojo();

		// Create and set a mock MavenProject
		MavenProject mockProject = new MavenProject();
		Build build = new Build();
		build.setFinalName("test-project");
		mockProject.setBuild(build);

		// Set the basedir in MavenProject using reflection
		Field basedirField = mockProject.getClass().getDeclaredField("basedir");
		basedirField.setAccessible(true);
		basedirField.set(mockProject, zipResourcePath.toFile());

		// Set up all required fields
		setField(mojo, "project", mockProject);
		setField(mojo, "outputDirectory", zipResourcePath.toFile());
		setField(mojo, "datasetPath", zipResourcePath.toFile());
		setField(mojo, "testResults", new String[] { "test_results/**/*.xml" });

		// Create mandatory requirements.yml file
		File reqFile = new File(zipResourcePath.toFile(), "requirements.yml");
		reqFile.getParentFile().mkdirs();
		reqFile.createNewFile();
		Files.write(reqFile.toPath(), "test content".getBytes());

		// Invoke the method
		Method assembleZipArtifactMethod = RequirementsToolMojo.class.getDeclaredMethod("assembleZipArtifact");
		assembleZipArtifactMethod.setAccessible(true);
		assembleZipArtifactMethod.invoke(mojo);

		// Only check if zip file exists
		assertTrue(Files.exists(zipResourcePath.resolve("test-project-reqstool.zip")));
	}

	@SVCs("SVC_MAVEN_PLUGIN_003")
	@Test
	void testAttachArtifactPassesAssembledZipUnderReqstoolClassifier(@TempDir File outputDirectory) throws Exception {
		MavenProjectHelper projectHelper = mock(MavenProjectHelper.class);
		RequirementsToolMojo mojo = newMojoForExecute(outputDirectory, projectHelper);
		setField(mojo, "skipAssembleZipArtifact", true);
		setField(mojo, "datasetPath", outputDirectory);

		File requirementsFile = new File(outputDirectory, "requirements.yml");
		Files.write(requirementsFile.toPath(), "requirements: []\n".getBytes());

		mojo.execute();

		Field projectField = RequirementsToolMojo.class.getDeclaredField("project");
		projectField.setAccessible(true);
		MavenProject project = (MavenProject) projectField.get(mojo);
		verify(projectHelper).attachArtifact(eq(project), eq("zip"), eq("reqstool"),
				eq(new File(outputDirectory, "test-project-reqstool.zip")));
	}

	@SVCs("SVC_MAVEN_PLUGIN_004")
	@Test
	void testSkipBypassesExecutionEntirely(@TempDir File outputDirectory) throws Exception {
		MavenProjectHelper projectHelper = mock(MavenProjectHelper.class);
		RequirementsToolMojo mojo = newMojoForExecute(outputDirectory, projectHelper);
		setField(mojo, "skip", true);

		mojo.execute();

		assertFalse(new File(outputDirectory, RequirementsToolMojo.OUTPUT_FILE_ANNOTATIONS_YML_FILE).exists());
		verifyNoInteractions(projectHelper);
	}

	@SVCs("SVC_MAVEN_PLUGIN_004")
	@Test
	void testSkipAssembleZipArtifactBypassesZipCreation(@TempDir File outputDirectory) throws Exception {
		MavenProjectHelper projectHelper = mock(MavenProjectHelper.class);
		RequirementsToolMojo mojo = newMojoForExecute(outputDirectory, projectHelper);
		setField(mojo, "skipAssembleZipArtifact", true);
		setField(mojo, "skipAttachZipArtifact", true);

		mojo.execute();

		assertTrue(new File(outputDirectory, RequirementsToolMojo.OUTPUT_FILE_ANNOTATIONS_YML_FILE).exists());
		assertFalse(new File(outputDirectory, "test-project-reqstool.zip").exists());
		verifyNoInteractions(projectHelper);
	}

	@SVCs("SVC_MAVEN_PLUGIN_004")
	@Test
	void testSkipAttachZipArtifactBypassesArtifactAttachment(@TempDir File outputDirectory) throws Exception {
		MavenProjectHelper projectHelper = mock(MavenProjectHelper.class);
		RequirementsToolMojo mojo = newMojoForExecute(outputDirectory, projectHelper);
		setField(mojo, "datasetPath", outputDirectory);
		setField(mojo, "skipAttachZipArtifact", true);

		File requirementsFile = new File(outputDirectory, "requirements.yml");
		Files.write(requirementsFile.toPath(), "requirements: []\n".getBytes());

		mojo.execute();

		assertTrue(new File(outputDirectory, "test-project-reqstool.zip").exists());
		verifyNoInteractions(projectHelper);
	}

}
