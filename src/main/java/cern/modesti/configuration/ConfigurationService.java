package cern.modesti.configuration;

import static java.lang.String.format;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.workflow.result.ConfigurationResult;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.modesti.request.Request;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ConfigurationService implements JavaDelegate {

  @Autowired
  RequestRepository requestRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * Map of request ids to their corresponding configuration listeners
   */
  private Map<String, ProgressUpdateListener> listeners = new HashMap<>();

  /**
   * Entry point to the configuration from the workflow engine. The 'configure' service task in the workflow refers to this class from a 'delegate expression',
   * which means that this method will be invoked as the service task.
   *
   * @param execution
   * @throws Exception
   */
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String requestId = execution.getProcessBusinessKey();

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    ConfigurationReport report = configureRequest(request, new ProgressUpdateListener());

    // OK, WARNING and RESTART are all considered successful
    boolean failure = report.getStatus() == ConfigConstants.Status.FAILURE;
    ConfigurationResult result;

    if (failure) {
      result = new ConfigurationResult(false);
      result.setErrors(Collections.singletonList(report.getStatusDescription()));
    } else {
      result = new ConfigurationResult(true);
    }

    request.setConfigurationResult(result);
    execution.setVariable("configured", !failure);

    // Store the request
    requestRepository.save(request);
  }

  /**
   * @param request
   *
   * @return true if the configuration was applied successfully, false otherwise
   */
  @Transactional
  public ConfigurationReport configureRequest(Request request, ProgressUpdateListener listener) {
    log.info(format("configuring points for request id %s...", request.getRequestId()));

    // Generate the configuration
    SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withCatalogName("CONFIG_GENERATOR").withProcedureName("STP_LOAD_POINT_LIST_WRAP");

    call.addDeclaredParameter(new SqlParameter("p_sql_text", OracleTypes.VARCHAR));
    call.addDeclaredParameter(new SqlOutParameter("p_config_id", OracleTypes.INTEGER));
    call.addDeclaredParameter(new SqlParameter("p_confname", OracleTypes.VARCHAR));
    call.addDeclaredParameter(new SqlParameter("p_confdesc", OracleTypes.VARCHAR));
    call.addDeclaredParameter(new SqlParameter("p_login_id", OracleTypes.NUMBER));
    call.addDeclaredParameter(new SqlOutParameter("p_exitcode", OracleTypes.INTEGER));
    call.addDeclaredParameter(new SqlOutParameter("p_exittext", OracleTypes.VARCHAR));

    String sql = format("SELECT mop_point_id FROM monpoints WHERE (mop_create_req_id = '%s' OR mop_change_req_id = '%s')", request.getRequestId(), request
        .getRequestId());

    Map<String, Object> result = call.execute(sql, format("MODESTI %s", request.getRequestId()), request.getDescription(), request.getCreator().getId());

    // Check the exit code
    Integer exitcode = (Integer) result.get("p_exitcode");
    String exittext = (String) result.get("p_exittext");

    if (exitcode != 0) {
      throw new ConfigurationException(format("Error generating configuration (%d): %s", exitcode, exittext));
    }

    Long configId = ((Integer) result.get("p_config_id")).longValue();
    listeners.put(request.getRequestId(), listener);

    C2monServiceGateway.startC2monClientSynchronous();

    // Apply the configuration
    ConfigurationReport report = C2monServiceGateway.getTagManager().applyConfiguration(configId, listener);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    listeners.remove(request.getRequestId());
    return report;
  }

  /**
   *
   * @param id
   * @return
   */
  public ProgressUpdateListener getProgressUpdateListener(String id) {
    return listeners.get(id);
  }
}
