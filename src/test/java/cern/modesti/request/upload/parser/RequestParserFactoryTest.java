package cern.modesti.request.upload.parser;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.Request;
import cern.modesti.request.upload.exception.RequestParseException;
import cern.modesti.test.plugin.DummyRequestParser;
import cern.modesti.test.plugin.DummyRequestProvider;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.plugin.core.PluginRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestParserFactoryTest {

  @InjectMocks
  RequestParserFactory requestParserFactory;

  @Mock
  ApplicationContext context;

  static Map<String, Resource> sheets = new HashMap<>();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    Resource[] sheets = resolver.getResources("classpath:/sheets/**/*.xls*");

    for (Resource sheet : sheets) {
      RequestParserFactoryTest.sheets.put(sheet.getFilename(), sheet);
    }
  }

  @Test(expected = RequestParseException.class)
  public void inputstreamParameterIsNull() throws IOException {
    requestParserFactory.parseRequest(null);
  }

  @Test(expected = UnsupportedRequestException.class)
  public void requestParserIsNull() throws IOException {
    Resource sheet = sheets.get("valid.xlsx");
    requestParserFactory.parseRequest(sheet.getInputStream());
  }

  @Test(expected = UnsupportedRequestException.class)
  public void invalidRequestDomainIsRejected() throws IOException {
    Resource sheet = sheets.get("invalid-domain.xlsx");
    requestParserFactory.parseRequest(sheet.getInputStream());
  }

  @Test(expected = RequestParseException.class)
  public void invalidFileTypeIsRejected() {
    InputStream stream = new ByteArrayInputStream("spam".getBytes());
    requestParserFactory.parseRequest(stream);
  }

  @Test(expected = RequestParseException.class)
  public void emptyExcelSheetIsRejected() throws IOException {
    when(context.getBeansOfType(RequestParser.class)).thenReturn(Collections.singletonMap("DUMMY", new DummyRequestParser()));
    Resource sheet = sheets.get("empty.xlsx");
    requestParserFactory.parseRequest(sheet.getInputStream());
  }


}
