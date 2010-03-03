package fitnesse.wikitext.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DownloaderTest {

  Downloader downloader = null;

  @Before
  public void setUp() throws Exception {
    downloader = new Downloader();
  }

  @Test
  public void shouldReturnsArtifactPlusDepedencies() throws Exception {
    List<String> artifacts = downloader
        .getArtifactAndDependencies("http://repo1.maven.org/maven2/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.pom");

    assertNotNull(artifacts);
    assertEquals(2, artifacts.size());

    artifacts = downloader.getArtifactAndDependencies("http://repo2.maven.org/maven2/org/springframework/spring-jdbc/2.5.6/spring-jdbc-2.5.6.pom");

    assertNotNull(artifacts);
    assertEquals(7, artifacts.size());

  }

  @Test
  public void shouldDownloadAPom() throws DownloadException {
    File res = downloader.downloadPom("http://repo1.maven.org/maven2/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.pom");
    assertNotNull(res);
    System.out.println(res.getAbsolutePath());
    assertTrue(res.exists());
    assertTrue(res.isFile());
    assertEquals("commons-beanutils-1.7.0.pom", res.getName());
  }
  
  @Test(expected=DownloadException.class)
  public void shouldSendExceptionWhenUnableToDownloadPom() throws DownloadException {
    downloader.downloadPom("http://serverdoesnotexist.com/maven2/undefined/undefined/999/undefined-999.pom");
  }
  
 
}
