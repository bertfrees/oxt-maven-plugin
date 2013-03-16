package be.docarch.maven.oxt;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;
import static org.twdata.maven.mojoexecutor.PlexusConfigurationUtils.toXpp3Dom;

/**
 * Create Manifest and build JAR for OpenOffice extension.
 *
 * @goal jar
 * @phase package
 */
public class JarMojo extends AbstractMojo {
	
	/**
	 * Directory containing the generated JAR.
	 *
	 * @parameter expression="${project.build.directory}/oxt"
	 * @required
	 */
	private File outputDirectory;
	
	/**
	 * Name of the generated JAR.
	 *
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 */
	private String finalName;
	
	/**
	 * List of files to include.
	 *
	 * @parameter
	 */
	protected XmlPlexusConfiguration includes;
	
	/**
	 * List of files to exclude.
	 *
	 * @parameter
	 */
	protected XmlPlexusConfiguration excludes;
	
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
	 * Comma separated list of files to include in the ClassPath.
	 * Specified as fileset patterns which are relative to outputDirectory.
	 *
	 * @parameter
	 */
	private String classPath = "";
	
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
		
		try {
			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setBasedir(outputDirectory.getAbsolutePath());
			scanner.setIncludes(StringUtils.split(classPath, ','));
			scanner.scan();
			
			Xpp3Dom configuration = configuration(
				element("outputDirectory", outputDirectory.getAbsolutePath()),
				element("finalName", finalName),
				element("archive",
					element("manifestEntries",
						element("Implementation-Title", "${project.artifactId}"),
						element("Implementation-Version", "${project.version}"),
						element("UNO-Type-Path", ""),
						element("RegistrationClassName", centralRegistrationClass),
						element("Class-Path", StringUtils.join(scanner.getIncludedFiles(), ' '))),
					element("manifestSections",
						element("manifestSection",
							element("name",
								centralRegistrationClass.trim().replaceAll("\\.", "/") + ".class"),
							element("manifestEntries",
								element("RegistrationClasses",
									registrationClasses.trim().replaceAll("\\s+", " ")))))));
			
			if (includes != null)
				configuration.addChild(toXpp3Dom(includes));
			if (excludes != null)
				configuration.addChild(toXpp3Dom(excludes));
			
			executeMojo(
				plugin(
					groupId("org.apache.maven.plugins"),
					artifactId("maven-jar-plugin"),
					version("2.4")),
				goal("jar"),
				configuration,
				executionEnvironment(
					project,
					session,
					pluginManager));
			
			project.getArtifact().setFile(null); }
		
		catch (Exception e) {
			throw new MojoExecutionException("Error generating JAR", e); }
	}
}
