package be.docarch.maven.oxt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import static org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * Create Manifest, build JAR and copy dependencies for OpenOffice extension.
 *
 * @goal jar
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class JarMojo extends AbstractMojo {
	
	/**
	 * Directory where the dependencies are copied.
	 *
	 * @parameter expression="${project.build.directory}/oxt"
	 * @required
	 */
	private File outputDirectory;
	
	/**
	 * Name of the central registration class.
	 *
	 * @parameter
	 * @required
	 */
	private String centralRegistrationClass;
	
	/**
	 * Space separated list of registration classes.
	 *
	 * @parameter
	 * @required
	 */
	private String registrationClasses;
	
	/**
	 * The project currently being build.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * The current Maven session.
	 *
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
    private MavenSession session;

	/**
	 * The Maven BuildPluginManager component.
	 *
	 * @component
	 * @required
	 */
	private BuildPluginManager pluginManager;

	public void execute() throws MojoExecutionException {
		
		File libDirectory = new File(outputDirectory, "lib");
		
		if (!libDirectory.exists()) {
			executeMojo(
				plugin(
					groupId("org.apache.maven.plugins"),
					artifactId("maven-dependency-plugin"),
					version("2.5")),
				goal("copy-dependencies"),
				configuration(
					element(name("outputDirectory"), libDirectory.getAbsolutePath()),
					element(name("includeScope"), "runtime")),
				executionEnvironment(
					project,
					session,
					pluginManager)); }
		
		String classpath = "";
		for (String file : libDirectory.list())
			classpath += "lib/" + file + " ";
		
		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-jar-plugin"),
				version("2.4")),
			goal("jar"),
			configuration(
				element(name("outputDirectory"), outputDirectory.getAbsolutePath()),
				element(name("archive"),
					element(name("manifestEntries"),
						element(name("Implementation-Title"), "${project.artifactId}"),
						element(name("Implementation-Version"), "${project.version}"),
						element(name("UNO-Type-Path"), ""),
						element(name("RegistrationClassName"), centralRegistrationClass),
						element(name("Class-Path"), classpath)),
					element(name("manifestSections"),
						element(name("manifestSection"),
							element(name("name"),
								centralRegistrationClass.trim().replaceAll("\\.", "/") + ".class"),
							element(name("manifestEntries"),
								element(name("RegistrationClasses"),
									registrationClasses.trim().replaceAll("\\s+", " "))))))),
			executionEnvironment(
				project,
				session,
				pluginManager));
				
		project.getArtifact().setFile(null);
	}
}
