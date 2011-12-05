package eu.vamdc.registry;

import java.util.List;
import org.astrogrid.registry.RegistryException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author Guy Rixon
 */
public class RegistryTest {

  @Test
  public void testFindTapXsams() throws Exception {
    Registry sut = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);
    Document results = sut.findTapXsams();
    assertNotNull(results);
    NodeList nl = results.getDocumentElement().getElementsByTagName("ri:Resource");
    assertNotNull(nl);
    assertTrue("No services found", nl.getLength() > 1);
  }

  @Test
  public void testFindVamdcTap() throws Exception {
    // Only expect to find these in the dev registry for the next few weeks - GTR 2011-06-02
    Registry sut = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);
    Document results = sut.findVamdcTap();
    assertNotNull(results);
    NodeList nl = results.getDocumentElement().getElementsByTagName("ri:Resource");
    assertNotNull(nl);
    assertTrue("No services found", nl.getLength() > 0);
  }

  @Test
  public void testFindTap() throws Exception {
    Registry sut = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);
    Document results = sut.findTap();
    assertNotNull(results);
    NodeList nl = results.getDocumentElement().getElementsByTagName("ri:Resource");
    assertNotNull(nl);
    assertTrue("No services found", nl.getLength() > 1);
  }

  @Test
  public void testFindWebSites() throws Exception {
    Registry sut = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);
    Document results = sut.findWebSites();
    assertNotNull(results);
    NodeList nl = results.getDocumentElement().getElementsByTagName("ri:Resource");
    assertNotNull(nl);
    assertTrue("No services found", nl.getLength() > 1);
  }

  @Test
  public void testExecuteXquery() throws Exception {
    Registry sut = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);
    // "Find all the registrations with a web-browser interface".
    // We expect the live registry to have at least one.
    String query =
        "declare namespace ri='http://www.ivoa.net/xml/RegistryInterface/v1.0';" +
        "declare namespace vr='http://www.ivoa.net/xml/VOResource/v1.0';" +
        "declare namespace xsi='http://www.w3.org/2001/XMLSchema-instance';" +
        "for $x in //ri:Resource " +
        "where $x/capability/interface[@xsi:type='vr:WebBrowser'] " +
        "and $x/@status='active' " +
        "return $x";
    Document results = sut.executeXquery(query);
    assertNotNull(results);
    NodeList nl = results.getDocumentElement().getElementsByTagName("ri:Resource");
    assertNotNull(nl);
    assertTrue("No services found", nl.getLength() > 1);
  }

  @Test
  public void findResourcesByCapability() throws Exception {
    Registry sut = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);
    Document results = sut.findResourcesByCapability(Registry.TAP_XSAMS_ID);
    Registry.serializeToStdout(results);
    assertNotNull(results);
    NodeList nl = results.getDocumentElement().getElementsByTagName("ri:Resource");
    assertNotNull(nl);
    assertTrue("No services found", nl.getLength() > 1);
  }

  @Test
  public void findIvornsByCapability() throws Exception {
    Registry sut = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);
    List<String> results = sut.findIvornsByCapability(Registry.VAMDC_TAP_ID);
    assertNotNull(results);
    assertTrue("No services found", results.size() > 1);
  }

  @Test
  public void findTapXsamsByXquery() throws Exception {
    System.out.println("\n\nfindTapXsamsByXquery()");
    Registry sut = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);
    // This query uses lower case for the returnable because that' what the
    // nodes are using right now. It should be mixed case as in the dictionary.
    String query =
        "declare namespace ri='http://www.ivoa.net/xml/RegistryInterface/v1.0'; " +
        "for $x in //ri:Resource " +
        "where $x/capability[@standardID='ivo://vamdc/std/TAP-XSAMS' " +
        "and returnable='radtranswavelengthexperimentalvalue'] " +
        "and $x/@status='active' " +
        "return $x/capability[@standardID='ivo://vamdc/std/TAP-XSAMS']/interface/accessURL";
    Document results = sut.executeXquery(query);
    assertNotNull(results);
    System.out.println("\nFindTapXsamsByQuery()");
    Registry.serializeToStdout(results);
    NodeList nl = results.getDocumentElement().getElementsByTagName("accessURL");
    assertNotNull(nl);
    assertTrue("No services found", nl.getLength() > 0);
  }

  @Test
  public void multipleRegistries() throws Exception {
    System.out.println("\n\nmultipleRegistries()");
    // The dev registry has a particular test fixture that is not present
    // in the level-1 registry. The default registry must be a release registry
    // so must not have the test fixture.

    Registry defaultRegistry = new Registry();
    Registry level1Registry  = new Registry(Registry.RELEASE_REGISTRY_ENDPOINT);
    Registry devRegistry     = new Registry(Registry.DEVELOPMENT_REGISTRY_ENDPOINT);

    try {
      Document defaultResult =
          defaultRegistry.getResource("ivo://vamdc/registry-client-test-fixture-1");
      fail("Should throw RegistryException for non-existant resource.");
    }
    catch (RegistryException e) {
      // Expected
    }

    try {
      Document level1Result =
          level1Registry.getResource("ivo://vamdc/registry-client-test-fixture-1");
      fail("Should throw RegistryException for non-existant resource.");
    }
    catch (RegistryException e) {
      // Expected
    }

    Document devResult =
        devRegistry.getResource("ivo://vamdc/registry-client-test-fixture-1");
    NodeList nl3 = devResult.getDocumentElement().getElementsByTagName("ri:Resource");
    assertEquals(1, nl3.getLength());
  }

}
