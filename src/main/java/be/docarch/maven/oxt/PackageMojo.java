package be.docarch.maven.oxt;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.archiver.zip.ZipArchiver;

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
 * Package the OpenOffice Extension (.oxt).
 *
 * @goal package
 * @phase package
 */
public class PackageMojo extends AbstractMojo {

	/**
	 * Name of the OXT file.
	 *
	 * @parameter default-value="${project.artifactId}-${project.version}"
	 * @required
	 */
	private String finalName;

	/**
	 * Directory containing the generated OXT.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

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

	/**
	 * The Jar archiver.
	 *
	 * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#zip}"
	 * @required
	 */
	private ZipArchiver zipArchiver;

	public void execute() throws MojoExecutionException {
		
		File oxtDirectory = new File(project.getBasedir(), "target/oxt");
		
		/* Generate Manifest */
		
		/* Package JAR */
		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-jar-plugin"),
				version("2.4")),
			goal("jar"),
			configuration(
				element(name("finalName"), finalName),
				element(name("outputDirectory"), oxtDirectory.getAbsolutePath())),
			executionEnvironment(
				project,
				session,
				pluginManager));
		
		/* Package OXT */
		try {
			File zipFile = new File(outputDirectory, finalName + ".oxt" );
			zipArchiver.setDestFile(zipFile);
			zipArchiver.setIncludeEmptyDirs(true);
			zipArchiver.setCompress(true);
			zipArchiver.setForced(true);
			zipArchiver.addDirectory(oxtDirectory);
			zipArchiver.createArchive();
			project.getArtifact().setFile(zipFile); }
		catch (Exception e) {
			throw new MojoExecutionException( "Error assembling OXT", e ); }
	}
}
