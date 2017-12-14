package gy.jk.datarecorder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import gy.jk.datarecorder.Annotations.LocalTradeHistory;
import gy.jk.proto.Shared.Exchange.Name;
import gy.jk.proto.Shared.Trade;

import java.util.Queue;

@Singleton
public class LocalDataStore implements DataStore {

  private final Queue<Trade> gdaxTradeHistory;

  @Inject
  LocalDataStore(@LocalTradeHistory Queue<Trade> gdaxTradeHistory) {
    this.gdaxTradeHistory = gdaxTradeHistory;
  }

  @Override
  public void recordTrade(Trade trade) {
    if (trade.hasExchange()
        && trade.getExchange().hasName()) {
      Name name = trade.getExchange().getName();
      switch (name) {
        case GDAX:
          gdaxTradeHistory.add(trade);
          break;
        case Bitstamp:
          break;
      }
    }
  }

  private boolean validTrade(Trade trade) {
    return trade.hasExchange() && trade.hasPrice() && trade.hasSize();
  }
}
