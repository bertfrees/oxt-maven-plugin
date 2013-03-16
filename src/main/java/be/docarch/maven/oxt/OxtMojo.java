package be.docarch.maven.oxt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFileFilter;

import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * Package the OpenOffice Extension (.oxt).
 *
 * @goal oxt
 * @phase package
 */
public class OxtMojo extends AbstractMojo {
	
	/**
	 * Directory that will contain the generated OXT.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;
	
	/**
	 * Name of the OXT file.
	 *
	 * @parameter default-value="${project.artifactId}-${project.version}"
	 * @required
	 */
	private String finalName;
	
	/**
	 * Classifier.
	 *
	 * @parameter
	 */
	private String classifier;
	
	/**
	 * Resources.
	 * 
	 * @parameter
	*/
	private Resource[] resources;
	
	/**
	 * Filter properties files for filtering the OXT resources.
	 *
	 * @parameter
	 */
	protected String[] filters;
	
	/**
	 * The character encoding scheme to be applied when filtering resources.
	 *
	 * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
	 */
	protected String encoding;
	
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
	 * Base directory of the project.
	 * 
	 * @parameter default-value="${project.basedir}"
	 * @required
	 * @readonly
	 */
	private File basedir;
	
	/**
	 * Temporary directory that contain the files to be assembled.
	 * 
	 * @parameter default-value="${project.build.directory}/archive-tmp"
	 * @required
	 * @readonly
	 */
	private File tempRoot;
	
	/**
	 * Maven ProjectHelper.
	 * 
	 * @component
	 */
	private MavenProjectHelper projectHelper;
	
	/**
	 * Maven shared filtering utility.
	 * 
	 * @component
	 */
	private MavenFileFilter mavenFileFilter;
	
	/**
	 * The ZIP archiver.
	 *
	 * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="zip"
	 */
	private ZipArchiver zipArchiver;
	
	public void execute() throws MojoExecutionException {
		
		try {
			
			if (resources == null) {
				resources = new Resource[] { new Resource(), new Resource() };
				resources[0].setDirectory("src/main/oxt");
				resources[0].setFiltering(false);
				resources[1].setDirectory("target/oxt");
				resources[1].setFiltering(false); }
			
			List<String> filterFiles = new ArrayList<String>();
			if (filters != null)
				for (String filter : filters)
					filterFiles.add(new File(basedir, filter).getAbsolutePath());
			
			for (Resource resource : resources) {
				DefaultFileSet fileset = new DefaultFileSet();
				fileset.setPrefix((resource.getTargetPath() == null) ? "" : (resource.getTargetPath() + "/"));
				File resourceDir = new File(basedir, resource.getDirectory());
				String[] includes = resource.getIncludes().toArray(new String[resource.getIncludes().size()]);
				String[] excludes = resource.getExcludes().toArray(new String[resource.getExcludes().size()]);
				if (resource.isFiltering()) {
					DirectoryScanner scanner = new DirectoryScanner();
					scanner.setBasedir(resourceDir);
					scanner.setIncludes(includes);
					scanner.setExcludes(excludes);
					scanner.scan();
					File filteredDir = FileUtils.createTempFile("package-oxt.", ".tmp", tempRoot);
					filteredDir.delete();
					filteredDir.mkdirs();
					for (String file : scanner.getIncludedFiles()) {
						File resourceFile = new File(resourceDir, file);
						File filteredFile = new File(filteredDir, file);
						filteredFile.getParentFile().mkdirs();
						mavenFileFilter.copyFile(resourceFile, filteredFile, true, project, filterFiles,
								false, encoding, session); }
					fileset.setDirectory(filteredDir); }
				else {
					fileset.setDirectory(resourceDir);
					fileset.setIncludes(includes);
					fileset.setExcludes(excludes); }
				zipArchiver.addFileSet(fileset); }
			
			zipArchiver.setCompress(true);
			zipArchiver.setForced(true);
			if (classifier != null)
				finalName += "-" + classifier;
			File zipFile = new File(outputDirectory, finalName + ".oxt" );
			zipArchiver.setDestFile(zipFile);
			zipArchiver.createArchive();
			
			if (classifier != null)
				projectHelper.attachArtifact(project, "oxt", classifier, zipFile);
			else
				project.getArtifact().setFile(zipFile); }
		
		catch (Exception e) {
			throw new MojoExecutionException("Error assembling OXT", e); }
	}
}
