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
 * Copy and filter resources for OpenOffice extension.
 *
 * @goal process-resources
 * @phase process-resources
 * @requiresDependencyResolution runtime
 */
public class ProcessResourcesMojo extends AbstractMojo {
	
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

		/* JAR resources (default) */
		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-resources-plugin"),
				version("2.5")),
			goal("resources"),
			configuration(),
			executionEnvironment(
				project,
				session,
				pluginManager));
		
		if (! new File(project.getBasedir(), "target/oxt").exists()) {
		
			/* OXT resources */
			List<Element> filters = new ArrayList<Element>();
			for (File file : new File(project.getBasedir(), "target/l10n/filters").listFiles())
				filters.add(element(name("filter"), file.getAbsolutePath()));
			executeMojo(
				plugin(
					groupId("org.apache.maven.plugins"),
					artifactId("maven-resources-plugin"),
					version("2.5")),
				goal("copy-resources"),
				configuration(
					element(name("encoding"), "UTF-8"),
					element(name("outputDirectory"), "${project.build.directory}/oxt"),
					element(name("filters"), filters.toArray(new Element[filters.size()])),
					element(name("resources"),
						element(name("resource"),
							element(name("directory"), "${basedir}/src/main/oxt"),
							element(name("filtering"), "false"),
							element(name("excludes"),
								element(name("exclude"), "Addons.xcu"),
								element(name("exclude"), "description.xml"),
								element(name("exclude"), "description/*"),
								element(name("exclude"), "META-INF/manifest.xml"))),
						element(name("resource"),
							element(name("directory"), "${basedir}/src/main/oxt"),
							element(name("filtering"), "true"),
							element(name("includes"),
								element(name("include"), "Addons.xcu"),
								element(name("include"), "description.xml"),
								element(name("include"), "description/*"),
								element(name("include"), "META-INF/manifest.xml"))))),
				executionEnvironment(
					project,
					session,
					pluginManager));
					
			/* Dependencies */
			executeMojo(
				plugin(
					groupId("org.apache.maven.plugins"),
					artifactId("maven-dependency-plugin"),
					version("2.5")),
				goal("copy-dependencies"),
				configuration(
					element(name("outputDirectory"), "${project.build.directory}/oxt/lib"),
					element(name("includeScope"), "runtime")),
				executionEnvironment(
					project,
					session,
					pluginManager)); }
	}
}
