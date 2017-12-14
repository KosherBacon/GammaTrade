package gy.jk.datarecorder;

import gy.jk.proto.Shared.Trade;

public interface DataStore {

  void recordTrade(Trade trade);

}
