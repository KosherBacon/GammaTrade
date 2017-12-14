package gy.jk.datarecorder;

import com.google.common.collect.EvictingQueue;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import gy.jk.datarecorder.Annotations.LocalTradeHistory;
import gy.jk.proto.Shared.Trade;

import java.util.Queue;

public class DataRecorderModule extends AbstractModule {

  private static final boolean USE_LOCAL_DATASTORE = true;
  private static final int MAX_TRADE_HISTORY = 10000000;

  @Override
  protected void configure() {
    if (USE_LOCAL_DATASTORE) {
      bind(DataStore.class).to(LocalDataStore.class);
    }
  }

  @Singleton
  @Provides
  @LocalTradeHistory
  @SuppressWarnings("unused")
  public Queue<Trade> tickers() {
    return EvictingQueue.create(MAX_TRADE_HISTORY);
  }
}
