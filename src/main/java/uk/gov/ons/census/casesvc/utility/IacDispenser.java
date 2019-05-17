package uk.gov.ons.census.casesvc.utility;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.casesvc.client.InternetAccessCodeSvcClient;

@Component
public class IacDispenser implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(IacDispenser.class);

  private final InternetAccessCodeSvcClient internetAccessCodeSvcClient;
  private final BlockingQueue<String> iacCodePool = new LinkedBlockingQueue<>();
  private boolean isFetchingIacCodes = false;

  @Value("${iacservice.pool-size-min}")
  private int iacPoolSizeMin;

  @Value("${iacservice.pool-size-max}")
  private int iacPoolSizeMax;

  public IacDispenser(InternetAccessCodeSvcClient internetAccessCodeSvcClient) {
    this.internetAccessCodeSvcClient = internetAccessCodeSvcClient;
  }

  public String getIacCode() {
    topUpPoolIfNecessary();

    try {
      return iacCodePool.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Waited too long for an IAC code - is the IAC service down?");
      throw new RuntimeException();
    }
  }

  // This function has to be synchronized to protect against a dead-heat request for a top-up
  private synchronized void topUpPoolIfNecessary() {
    // Theoretically we could end up with more threads waiting for IAC codes than we've requested
    // so we should never have more worker threads than the max pool size.
    if (!isFetchingIacCodes && iacCodePool.size() < iacPoolSizeMin) {
      isFetchingIacCodes = true; // Don't ask for more codes until the last request has completed

      Thread thread = new Thread(this);
      thread.run();
    }
  }

  @Override
  public void run() {
    try {
      // In theory this could fail, but it's retryable and it will recover so long as requests for
      // IAC codes continue to arrive. In the worst case scenario messages will build up on the
      // Rabbit queue until somebody spots the errors and resolves the issue
      List<String> generatedIacCodes = internetAccessCodeSvcClient.generateIACs(iacPoolSizeMax);

      iacCodePool.addAll(generatedIacCodes);
    } catch (Exception exception) {
      // This is more of a warning because it's recoverable but it can cause an error
      log.error("Unexpected exception when requesting IAC codes to top up pool", exception);

      // No point throwing an exception here, because we are on a different thread
      // we will have to wait for the threads that are waiting timeout
    } finally {
      isFetchingIacCodes = false;
    }
  }
}