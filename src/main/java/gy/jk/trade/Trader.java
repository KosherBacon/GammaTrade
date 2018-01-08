package gy.jk.trade;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.inject.Inject;
import gy.jk.orderengine.OrderEngine;
import gy.jk.strategy.StrategyBuilder;
import gy.jk.trade.Annotations.TradeStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchange.dto.Order.OrderType;
import org.ta4j.core.Strategy;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Joshua Kahn
 */
public class Trader {

  private static final Logger LOG = LogManager.getLogger();

  private final Strategy strategy;
  private final TimeSeries timeSeries;
  private final OrderEngine orderEngine;
  private final ListeningScheduledExecutorService executorService;

  @Inject
  Trader(@TradeStrategy StrategyBuilder strategyBuilder,
      TimeSeries timeSeries,
      OrderEngine orderEngine,
      ListeningScheduledExecutorService executorService) {
    this.strategy = strategyBuilder.buildStrategy(timeSeries);

    this.timeSeries = timeSeries;
    this.orderEngine = orderEngine;
    this.executorService = executorService;

    LOG.info("Using strategy: {}", strategyBuilder.getClass().getSimpleName());
    LOG.info("OrderEngine provided by: {}", orderEngine.getClass().getSimpleName());
  }

  public synchronized void receiveAndProcessTick(Tick tick) {
    if (tick.getTrades() == 0) {
      LOG.info("Tick contained no trades, skipping.");
      return;
    }
    timeSeries.addTick(tick);

    int endIndex = timeSeries.getEndIndex();
    if (strategy.shouldEnter(endIndex)) {
      LOG.info("Strategy indicates BUY.");
//      if (lastOrder.type == OrderType.ASK || lastOrder.type == null) {
      executeBuy();
//      } else {
//        LOG.info("Strategy indicated BUY with BUY as last trade.");
//      }
    } else if (strategy.shouldExit(endIndex)) {
      LOG.info("Strategy indicates SELL.");
//      if (lastOrder.type == OrderType.BID || lastOrder.type == null) {
      executeSell();
//      } else {
//        LOG.info("Strategy indicated SELL with SELL as last trade.");
//      }
    } else {
      LOG.info("Strategy indicates WAIT.");
    }
  }

  /**
   * Buy the using the counter currency, typically USD.
   */
  private void executeBuy() {
    ListenableFuture<Optional<String>> orderIdOptional = orderEngine.placeOrder(OrderType.BID);
    orderIdOptional = Futures.withTimeout(orderIdOptional, 5000, TimeUnit.MILLISECONDS,
        executorService);
    Futures.addCallback(orderIdOptional, new FutureCallback<Optional<String>>() {
      @Override
      public void onSuccess(Optional<String> result) {
        LOG.info("Bought with ID: {}", result.orElse("No Order Necessary"));
      }

      @Override
      public void onFailure(Throwable t) {
        LOG.error("BUY order failed!", t);
      }
    });
  }

  /**
   * Sell using the base currency, typically BTC.
   */
  private void executeSell() {
    ListenableFuture<Optional<String>> orderIdOptional = orderEngine.placeOrder(OrderType.ASK);
    orderIdOptional = Futures.withTimeout(orderIdOptional, 5000, TimeUnit.MILLISECONDS,
        executorService);
    Futures.addCallback(orderIdOptional, new FutureCallback<Optional<String>>() {
      @Override
      public void onSuccess(Optional<String> result) {
        LOG.info("Sold with ID: {}", result.orElse("No Order Necessary"));
      }

      @Override
      public void onFailure(Throwable t) {
        LOG.error("BUY order failed!", t);
      }
    });
  }
}
