package cern.modesti.request.upload.parser;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.Request;
import cern.modesti.request.upload.exception.RequestParseException;
import cern.modesti.test.plugin.DummyRequestProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.plugin.core.PluginRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.mockito.Mockito.when;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestParserFactoryTest {

  @InjectMocks
  RequestParserFactory requestParserFactory;

  @Mock
  PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  static Map<String, Resource> sheets = new HashMap<>();

  List<RequestProvider> requestProviders = new ArrayList<>(Collections.singletonList(new DummyRequestProvider()));

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    Resource[] sheets = resolver.getResources("classpath:/sheets/**/*.xls*");

    for (Resource sheet : sheets) {
      RequestParserFactoryTest.sheets.put(sheet.getFilename(), sheet);
    }
  }

  @Test(expected = UnsupportedRequestException.class)
  public void invalidRequestDomainIsRejected() throws IOException {
    Resource sheet = sheets.get("invalid-domain.xlsx");
    requestParserFactory.parseRequest(sheet.getInputStream());
  }

  @Test(expected = RequestParseException.class)
  public void invalidRequestTypeIsRejected() throws IOException {
    when(requestProviderRegistry.getPlugins()).thenReturn(requestProviders);

    Resource sheet = sheets.get("invalid-request-type.xlsx");
    requestParserFactory.parseRequest(sheet.getInputStream());
  }

  @Test(expected = RequestParseException.class)
  public void invalidFileTypeIsRejected() {
    InputStream stream = new ByteArrayInputStream("spam".getBytes());
    requestParserFactory.parseRequest(stream);
  }

  @Test(expected = RequestParseException.class)
  public void emptyExcelSheetIsRejected() throws IOException {
    when(requestProviderRegistry.getPlugins()).thenReturn(requestProviders);

    Resource sheet = sheets.get("empty.xlsx");
    requestParserFactory.parseRequest(sheet.getInputStream());
  }
}
