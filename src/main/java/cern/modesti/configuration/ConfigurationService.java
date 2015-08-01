package cern.modesti.configuration;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;

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
public class ConfigurationService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * Map of request ids to their corresponding configuration listeners
   */
  private Map<String, ProgressUpdateListener> listeners = new HashMap<>();

  /**
   * @param request
   *
   * @return true if the configuration was applied successfully, false otherwise
   */
  @Transactional
  public ConfigurationReport configureRequest(Request request, ProgressUpdateListener listener) {

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
