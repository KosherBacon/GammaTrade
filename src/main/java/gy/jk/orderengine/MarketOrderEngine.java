package gy.jk.orderengine;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import org.knowm.xchange.dto.Order;

import java.util.Optional;

public class MarketOrderEngine extends OrderEngine {

  @Inject
  MarketOrderEngine() {
  }

  @Override
  public ListenableFuture<Optional<String>> placeOrder(Order.OrderType orderType) {
    return Futures.immediateFuture(Optional.empty());
  }
}
