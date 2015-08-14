package cern.modesti.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RestController
@Slf4j
@Profile({"dev", "prod"})
public class ProgressUpdateController {

  @Autowired
  ConfigurationService configurationService;

  @RequestMapping(value = "/requests/{id}/progress", method = GET)
  public ProgressUpdate getProgress(@PathVariable("id") String id) {
    ProgressUpdateListener listener = configurationService.getProgressUpdateListener(id);

    if (listener != null) {
      return listener.getProgress();
    }

    return null;
  }
}
