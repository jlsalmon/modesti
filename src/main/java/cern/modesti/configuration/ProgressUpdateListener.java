package cern.modesti.configuration;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import lombok.Data;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
public class ProgressUpdateListener implements ClientRequestReportListener {

  private ProgressUpdate progress = new ProgressUpdate();

  @Override
  public void onProgressReportReceived(ClientRequestProgressReport progressReport) {
    progress.setAction(progressReport.getProgressDescription());

    if (progressReport.getTotalProgressParts() > 0) {
      progress.setProgress((100 * progressReport.getCurrentProgressPart()) / progressReport.getTotalProgressParts());
    } else {
      progress.setProgress(100);
    }
  }

  @Override
  public void onErrorReportReceived(ClientRequestErrorReport errorReport) {
    //errorReport.
  }
}
