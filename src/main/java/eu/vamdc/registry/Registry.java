package eu.vamdc.registry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.astrogrid.config.SimpleConfig;
import org.astrogrid.registry.RegistryException;
import org.astrogrid.registry.client.query.v1_0.QueryRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * Client for querying the registry. This class encapsulates the SOAP
 * web-service interface to the VAMDC registry, providing an API that is
 * simpler and more appropriate for the VAMDC.
 * <p>
 * Some assumptions are made about the registry: it responds to XQueries; it
 * has the particular behaviour under query of an AstroGrid implementation of
 * the IVOA registry-standards; there are few enough registrations that DOM
 * results are safe.
 * All these are reasonable for the VAMDC registry as it exists in 2010.
 * <p>
 * A Registry constructed with the no-argument constructor talks to the registry
 * service for the most-recent VAMDC release. To use a specific registry, specify
 * the URL for the SOAP query-interface in the call to the constructor. The
 * URLs for known registries are {@link #RELEASE_REGISTRY_ENDPOINT} and
 * {@link #DEVELOPMENT_REGISTRY_ENDPOINT}.
 * <p>
 * There are various methods of querying.
 * <dl>
 * <dt>Select whole registration documents by capability</dt>
 * <dd>Call {@link #findTapXsams}, {@link #findTap}, {@link #findWebSites} or
 * {@link #findResourcesByCapability} to get a {@code Document}, then
 * dismantle the document using DOM APIs. The returned document contains zero
 * or more registrations documents as first-level children.</dd>
 * <dt>Select access URLs by capability</dt>
 * <dd>Call {@link #findAccessUrlsByCapability} to get a list of URLs.
 * <dt>Make a custom XQuery</dt>
 * <dd>Call {@link #executeXquery} to send the query and get a {@code Document},
 * then dismantle the results with DOM calls. The results of the query will be
 * the first-level children of the returned document.
 * </ul>
 * @author Guy Rixon
 */
public class Registry {

  /**
   * Standard identifier for the class of services of interest.
   */
  public final static String TAP_XSAMS_ID = "ivo://vamdc/std/TAP-XSAMS";

  /**
   * Standard identifier for the class of services of interest.
   */
  public final static String VAMDC_TAP_ID = "ivo://vamdc/std/VAMDC-TAP";

  /**
   * Standard identifier for the class of services of interest.
   */
  public final static String TAP_ID = "ivo://ivoa.net/std/TAP";

  /**
   * Where the registry queries are sent for the level-1 release.
   */
  public final static String RELEASE_REGISTRY_ENDPOINT =
      "http://registry.vamdc.eu/vamdc_registry/services/RegistryQueryv1_0";
  
  /**
   * Where the registry queries are sent by default. A Registry constructed
   * with the no-argument constructor uses this location.
   */
  public final static String DEFAULT_REGISTRY_ENDPOINT = RELEASE_REGISTRY_ENDPOINT;

  /**
   * Where the registry queries are sent for the development system.
   */
  public final static String DEVELOPMENT_REGISTRY_ENDPOINT =
      "http://casx019-zone1.ast.cam.ac.uk/registry/services/RegistryQueryv1_0";

  /**
   * Where the registry queries are sent.
   */
  private final String endpoint;

  /**
   * Constructs a client for the default registry.
   */
  public Registry() {
    this(DEFAULT_REGISTRY_ENDPOINT);
  }


  /**
   * Constructs a client for a registry at a given location.
   *
   * @param endpoint The URL for the SOAP endpoint of the registry query.
   */
  public Registry(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * Supplies the registration documents for all services registered with
   * a TAP-XSAMS capability. All these documents are combined in one document,
   * with the registrations as first-level children of the latter.
   *
   * @return The registrations, combined in one document.
   * @throws RegistryException If the registry cannot fulfill the request.
   * @deprecated Use {@link #findVamdcTap} instead.
   */
  public Document findTapXsams() throws RegistryException {
    SimpleConfig.setProperty("return.soapbody", "true");
    QueryRegistry reggie = new QueryRegistry(registryEndpoint());
    String query =
        "declare namespace ri='http://www.ivoa.net/xml/RegistryInterface/v1.0';" +
        "for $x in //ri:Resource " +
        "where $x/capability[@standardID='" + TAP_XSAMS_ID + "'] " +
        "and $x/@status='active' " +
        "return $x";
    return reggie.xquerySearch(query);
  }

  /**
   * Supplies the registration documents for all services registered with
   * a TAP-XSAMS capability. All these documents are combined in one document,
   * with the registrations as first-level children of the latter.
   *
   * @return The registrations, combined in one document.
   * @throws RegistryException If the registry cannot fulfill the request.
   */
  public Document findVamdcTap() throws RegistryException {
    SimpleConfig.setProperty("return.soapbody", "true");
    QueryRegistry reggie = new QueryRegistry(registryEndpoint());
    String query =
        "declare namespace ri='http://www.ivoa.net/xml/RegistryInterface/v1.0';" +
        "for $x in //ri:Resource " +
        "where $x/capability[@standardID='" + VAMDC_TAP_ID + "'] " +
        "and $x/@status='active' " +
        "return $x";
    return reggie.xquerySearch(query);
  }

  /**
   * Supplies the registration documents for all services registered with
   * a TAP capability. All these documents are combined in one document,
   * with the registrations as first-level children of the latter.
   *
   * @return The registrations, combined in one document.
   * @throws RegistryException If the registry cannot fulfill the request.
   */
  public Document findTap() throws RegistryException {
    SimpleConfig.setProperty("return.soapbody", "true");
    QueryRegistry reggie = new QueryRegistry(registryEndpoint());
    String query =
        "declare namespace ri='http://www.ivoa.net/xml/RegistryInterface/v1.0';" +
        "for $x in //ri:Resource " +
        "where $x/capability[@standardID='" + TAP_ID + "'] " +
        "and $x/@status='active' " +
        "return $x";
    return reggie.xquerySearch(query);
  }

  /**
   * Supplies the registration documents for all services registered with
   * a capability containing a web-browser interface. All these documents are
   * combined in one document, with the registrations as first-level children
   * of the latter.
   *
   * @return The registrations, combined in one document.
   * @throws RegistryException If the registry cannot fulfill the request.
   */
  public Document findWebSites() throws RegistryException {
    String query =
        "declare namespace ri='http://www.ivoa.net/xml/RegistryInterface/v1.0';" +
        "declare namespace vr='http://www.ivoa.net/xml/VOResource/v1.0';" +
        "declare namespace xsi='http://www.w3.org/2001/XMLSchema-instance';" +
        "for $x in //ri:Resource " +
        "where $x/capability/interface[@xsi:type='vr:WebBrowser'] " +
        "and $x/@status='active' " +
        "return $x";
    return executeXquery(query);
  }

  /**
   * Supplies the registration document for the service with the given IVORN.
   *
   * @return The registrations document.
   * @throws RegistryException If the registry cannot fulfill the request.
   */
  public Document getResource(String ivorn) throws RegistryException {
    QueryRegistry reggie = new QueryRegistry(registryEndpoint());
    return reggie.getResourceByIdentifier(ivorn);
  }

  /**
   * Lists services registered with a given capability. If there are no such
   * services, the returned list is empty.
   * 
   * @param capabilityId The standard identifier for the capability.
   * @return The IVORNs of services with that capability (never null, list may be empty).
   * @throws RegistryException If the registry breaks.
   */
  public List<String> findIvornsByCapability(String capabilityId) throws RegistryException {
    SimpleConfig.setProperty("return.soapbody", "true");
    QueryRegistry reggie = new QueryRegistry(registryEndpoint());
    String query =
        "declare namespace ri='http://www.ivoa.net/xml/RegistryInterface/v1.0';" +
        "for $x in //ri:Resource " +
        "where $x/capability[@standardID='" + capabilityId + "'] " +
        "and $x/@status='active' " +
        "return $x/identifier";
    Document results = reggie.xquerySearch(query);

    // for debugging serializeToStdout(results);

    List<String> services = new ArrayList<String>();
    Element de = results.getDocumentElement();
    if (de != null) {
      NodeList nl2 = de.getElementsByTagName("identifier");
      for (int i = 0; i < nl2.getLength(); i++) {
        Node n = nl2.item(i);
        // for debugging serializeToStdout((Element) n);
        String ivorn = n.getTextContent();
        services.add(ivorn);
      }
    }
    return services;
  }

  public Document findResourcesByCapability(String capabilityId) throws RegistryException {
    String query =
        "declare namespace ri='http://www.ivoa.net/xml/RegistryInterface/v1.0';" +
        "for $x in //ri:Resource " +
        "where $x/capability[@standardID='" + capabilityId + "'] " +
        "and $x/@status='active' " +
        "return $x";
    return executeXquery(query);
  }

  /**
   * Supplies an access URL for the given capability of the service with the
   * given IVORN. The first URL in the first interface of that capability is
   * returned; all other URLs and interfaces are ignored.
   *
   * @param ivorn The IVORN for the service of choice.
   * @param capabilityId The identifier for the capability of choice.
   * @return The URL.
   * @throws RegistryException If the registry cannot fulfill the request.
   */
  public String findAccessUrl(String ivorn, String capabilityId) throws RegistryException {
    QueryRegistry reggie = new QueryRegistry(registryEndpoint());
    return reggie.getEndpointByIdentifier(ivorn, capabilityId);
  }

  /**
   * Supplies an access URL for each service having a registered capability
   * of the given type. Where such a capability has multiple interfaces, or
   * where an interface has multiple access URLS, only the first is returned;
   * the others are ignored.
   *
   * @param capabilityId The capability of choice.
   * @return The access URLs.
   * @throws RegistryException If the registry cannot fulfill the query.
   */
  public Set<String> findAccessUrlsByCapability(String capabilityId) throws RegistryException {
    List<String> ivorns = findIvornsByCapability(capabilityId);
    Set<String> services = new HashSet<String>(ivorns.size());
    for (String ivorn : ivorns) {
      services.add(findAccessUrl(ivorn, capabilityId));
    }
    return services;
  }

  /**
   * Executes the given XQuery and returns the results as a DOM. That DOM
   * contains an unspecified top-level element containing the nodes raised
   * by the query as first-level children. E.g., if the query returns whole
   * registration documents, the {@code ri:Resource} nodes of those documents
   * are the first-level children.
   * <p>
   * If the XQuery is invalid (bad syntax or missing namespace declarations) or
   * simply returns no elements, the results are completely unpredictable. Don't
   * expect useful error-messages.
   *
   * @param query The XQuery.
   * @return The DOM containing nodes matching the query.
   * @throws RegistryException If the registry cannot fulfill the query.
   */
  public Document executeXquery(String query) throws RegistryException {
    SimpleConfig.setProperty("return.soapbody", "true");
    QueryRegistry reggie = new QueryRegistry(registryEndpoint());
    return reggie.xquerySearch(query);
  }

  /**
   * Supplies the endpoint for the registry query.
   * 
   * @return The endpoint.
   */
  private URL registryEndpoint() {
    try {
      return new URL(endpoint);
    }
    // If the code above throws, it means a coding error in this class, since
    // URL is hard-coded above.
    catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Serializes the given document to standard output for debugging purposes.
   *
   * @param d The document.
   */
  public static void serializeToStdout(Document d) {
    try {
      DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
      LSSerializer writer = impl.createLSSerializer();
      String str = writer.writeToString(d);
      System.out.println(str);
    }
    catch (ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
    catch (InstantiationException ex) {
      throw new RuntimeException(ex);
    }
    catch (IllegalAccessException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Serializes the given element to standard output for debugging purposes.
   *
   * @param e The element.
   */
  public static void serializeToStdout(Element e) {
    try {
      DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
      LSSerializer writer = impl.createLSSerializer();
      String str = writer.writeToString(e);
      System.out.println(str);
    }
    catch (ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
    catch (InstantiationException ex) {
      throw new RuntimeException(ex);
    }
    catch (IllegalAccessException ex) {
      throw new RuntimeException(ex);
    }
  }

}
