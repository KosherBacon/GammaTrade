package jk.gy;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class TestHelper {

  public static final ListeningExecutorService TEST_EXECUTOR_SERVICE =
      MoreExecutors.newDirectExecutorService();

}
