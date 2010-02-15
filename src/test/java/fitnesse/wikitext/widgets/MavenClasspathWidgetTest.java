package fitnesse.wikitext.widgets;

import static fitnesse.html.HtmlUtil.BRtag;
import static fitnesse.html.HtmlUtil.metaText;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;

import fitnesse.wiki.PageData;

public class MavenClasspathWidgetTest extends WidgetTestCase {
  private static final String TEST_PROJECT_ROOT = new File("src/test/resources/MavenClasspathWidget").getAbsolutePath();
  private final static String TEST_POM_FILE = "src/test/resources/MavenClasspathWidget/pom.xml";
  private File mavenLocalRepo = new File(System.getProperty("java.io.tmpdir"), "MavenClasspathWidgetTest/m2/repo");
  private String path = mavenLocalRepo.getAbsolutePath();

  private MavenClasspathWidget widget;
  private Downloader downloader;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    widget = new MavenClasspathWidget(new MockWidgetRoot(), "!pomFile " + TEST_POM_FILE);
    downloader = new Downloader();
    downloader.localRepository = mavenLocalRepo;
    widget.setDownloader(downloader);
    mavenLocalRepo.mkdirs();
    FileUtils.copyDirectoryStructure(new File(TEST_PROJECT_ROOT, "repository"), mavenLocalRepo);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    FileUtils.deleteDirectory(mavenLocalRepo);
  }

  @Override
  protected String getRegexp() {
    return MavenClasspathWidget.REGEXP;
  }

  public void testThatMavenClasspathWidgetIsAddedToTheClasspathWidgetBuilder() throws Exception {
    assertEquals(MavenClasspathWidget.class, PageData.classpathWidgetBuilder.findWidgetClassMatching("!pomFile pom.xml"));
  }

  public void testRegexp() throws Exception {
    assertMatchEquals("!pomFile pom.xml", "!pomFile pom.xml");
  }

  public void testAsWikiText() throws Exception {
    assertEquals("!pomFile " + TEST_POM_FILE, widget.asWikiText());
  }

  public void testRender() throws Exception {
    String actual = widget.render();
    String expected = metaText("classpath: " + TEST_PROJECT_ROOT + "/target/classes") + BRtag
        + metaText(classpathElementForRender("/fitnesse/fitnesse-dep/1.0/fitnesse-dep-1.0.jar")) + BRtag
        + metaText(classpathElementForRender("/fitnesse/fitnesse-subdep/1.0/fitnesse-subdep-1.0.jar")) + BRtag;
    assertEquals(expected, actual);
  }

  public void testGetText() throws Exception {
    String actual = widget.getText();
    String expected = TEST_PROJECT_ROOT + "/target/classes" + classpathElementForText("/fitnesse/fitnesse-dep/1.0/fitnesse-dep-1.0.jar")
        + classpathElementForText("/fitnesse/fitnesse-subdep/1.0/fitnesse-subdep-1.0.jar");
    assertEquals(expected, actual);
  }

  public void testFailFastWhenPomFileDoesNotExist() throws Exception {
    try {
      new MavenClasspathWidget(new MockWidgetRoot(), "!pomFile /non/existing/pom.xml");
      fail("should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testFailFastWhenPomFileIsNotSpecified() throws Exception {
    try {
      new MavenClasspathWidget(new MockWidgetRoot(), "!pomFile");
      fail("should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testShouldReplaceVariablesInPath() throws Exception {
    System.setProperty("MY_PATH", "src/test/resources");
    widget = new MavenClasspathWidget(new MockWidgetRoot(), "!pomFile ${MY_PATH}/MavenClasspathWidget/pom.xml");
    widget.setDownloader(downloader);

    String html = widget.childHtml();
    assertEquals(TEST_POM_FILE, html);
  }

  private String classpathElementForRender(String file) {
    return "classpath: " + path + file;
  }

  private String classpathElementForText(String file) {
    return File.pathSeparator + path + file;
  }

  public void testRenderWithPomAsUrl() throws Exception {
    // this is a test with a real pom on the maven server, so it won't work offline
    // TODO : use a local server
    widget = new MavenClasspathWidget(new MockWidgetRoot(), "!pomFile "
        + "http://repo2.maven.org/maven2/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.pom");
    widget.setDownloader(downloader);
    String actual = widget.render();
    System.out.println(actual);
  }
  
  public void testShouldTryToReadLocalPomIfURLIsNotAccessible() throws Exception{
    String url = "http://servernotexist/artifactnotexist.pom";
    widget = new MavenClasspathWidget(new MockWidgetRoot(), "!pomFile "
        + url);
    widget.setDownloader(downloader);
    
    String actual = widget.render();
    assertEquals("Error : unable to download pom, check the url and connexion settings. url="+url, actual);
  }
}
