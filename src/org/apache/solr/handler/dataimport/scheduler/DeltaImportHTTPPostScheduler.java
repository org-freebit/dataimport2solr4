package org.apache.solr.handler.dataimport.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeltaImportHTTPPostScheduler extends BaseTimerTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DeltaImportHTTPPostScheduler.class);

	public DeltaImportHTTPPostScheduler(String webAppName) throws Exception {
		super(webAppName);
		logger.info("<index update process> DeltaImportHTTPPostScheduler init");
	}

    @Override
    public void run() {
        try {
            if (!this.server.isEmpty() && !this.webapp.isEmpty() && this.params != null && !this.params.isEmpty()) {
                if (this.singleCore) {
                    this.prepUrlSendHttpPost(this.params);
                } else if (this.syncCores.length != 0 && (this.syncCores.length != 1 || !this.syncCores[0].isEmpty())) {
                    String[] var4 = this.syncCores;
                    int var3 = this.syncCores.length;

                    for (int var2 = 0; var2 < var3; ++var2) {
                        String core = var4[var2];
                        this.prepUrlSendHttpPost(core, this.params);
                    }
                } else {
                    logger.warn("<index update process> No cores scheduled for data import");
                    logger.info("<index update process> Reloading global dataimport.properties");
                    this.reloadParams();
                }
            } else {
                logger.warn("<index update process> Insuficient info provided for data import");
                logger.info("<index update process> Reloading global dataimport.properties");
                this.reloadParams();
            }
        } catch (Exception var5) {
            logger.error("Failed to prepare for sendHttpPost", var5);
            this.reloadParams();
        }

    }
}
