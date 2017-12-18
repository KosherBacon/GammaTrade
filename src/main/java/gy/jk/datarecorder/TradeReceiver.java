package gy.jk.datarecorder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import gy.jk.proto.Shared.Trade;
import gy.jk.tick.TickEngine;

@Singleton
public class TradeReceiver {

  private final DataStore dataStore;
  private final TickEngine tickEngine;

  @Inject
  TradeReceiver(DataStore dataStore, TickEngine tickEngine) {
    this.dataStore = dataStore;
    this.tickEngine = tickEngine;
  }

  public void newTrade(Trade trade) {
//    dataStore.recordTrade(trade);
    tickEngine.receiveTrade(trade);
  }
}
