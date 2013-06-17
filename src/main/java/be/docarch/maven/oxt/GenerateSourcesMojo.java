package be.docarch.maven.oxt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Generate central registration class
 *
 * @goal generate-sources
 * @phase generate-sources
 */
public class GenerateSourcesMojo extends AbstractMojo {
	
	private static final String centralRegistrationClass = "be.docarch.maven.oxt.CentralRegistration";
	
	/**
	 * Directory containing the generated sources.
	 *
	 * @parameter expression="${project.build.directory}/generated-sources/oxt-maven-plugin"
	 * @read-only
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
	
	public void execute() throws MojoExecutionException {
		try {
			unpack(
				GenerateSourcesMojo.class.getResource("/" + centralRegistrationClass.replaceAll("\\.", "/") + ".java"),
				new File(outputDirectory, centralRegistrationClass.replaceAll("\\.", "/") + ".java"));
			project.addCompileSourceRoot(outputDirectory.getAbsolutePath()); }
		catch (Exception e) {
			throw new MojoExecutionException("Error generating central registration class", e); }
	}
	
	public static boolean unpack(URL url, File file) {
		if (file.exists()) return false;
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream writer = new FileOutputStream(file);
			url.openConnection();
			InputStream reader = url.openStream();
			byte[] buffer = new byte[153600];
			int bytesRead = 0;
			while ((bytesRead = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600]; }
			writer.close();
			reader.close();
			return true; }
		catch (Exception e) {
			throw new RuntimeException("Exception occured during unpacking of file '" + file.getName() + "'", e); }
	}
}
