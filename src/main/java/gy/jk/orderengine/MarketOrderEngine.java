package gy.jk.orderengine;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import gy.jk.exchange.TradingApi;
import gy.jk.trade.Annotations.MaximumOrderSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchange.dto.Order.OrderType;

import java.math.BigDecimal;
import java.util.Optional;

public class MarketOrderEngine extends OrderEngine {

  private static final Logger LOG = LogManager.getLogger();

  private final TradingApi tradingApi;
  private final BigDecimal maximumOrderSize;
  private final OrderState lastOrder;

  @Inject
  MarketOrderEngine(TradingApi tradingApi,
      @MaximumOrderSize BigDecimal maximumOrderSize) {
    this.tradingApi = tradingApi;
    this.maximumOrderSize = maximumOrderSize;
    lastOrder = new OrderState();
  }

  @Override
  public ListenableFuture<Optional<String>> placeOrder(OrderType orderType) {
    if (orderType == OrderType.BID) {
      LOG.info("Executing buy order on {}", tradingApi.getMarketName());
    } else {
      LOG.info("Executing sell order on {}", tradingApi.getMarketName());
    }

    return Futures.immediateFuture(Optional.empty());
  }
}
