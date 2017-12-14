package gy.jk.trade;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import gy.jk.exchange.TradingApi;
import gy.jk.strategy.StrategyBuilder;
import gy.jk.trade.Annotations.MaximumOrderSize;
import gy.jk.trade.Annotations.TradeStrategy;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Strategy;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Joshua Kahn
 */
public class Trader {

  private static final Logger LOG = LoggerFactory.getLogger(Trader.class);

  private final Strategy strategy;
  private final TimeSeries timeSeries;
  private final TradingApi tradingApi;
  private final CurrencyPair currencyPair;
  private final ScheduledExecutorService executorService;
  private final BigDecimal maximumOrderSize;

  private OrderState lastOrder;

  @Inject
  Trader(@TradeStrategy StrategyBuilder strategyBuilder, TimeSeries timeSeries,
      TradingApi tradingApi, CurrencyPair currencyPair, ScheduledExecutorService executorService,
      @MaximumOrderSize BigDecimal maximumOrderSize) {
    this.strategy = strategyBuilder.buildStrategy(timeSeries);

    this.timeSeries = timeSeries;
    this.tradingApi = tradingApi;
    this.currencyPair = currencyPair;
    this.executorService = executorService;
    this.maximumOrderSize = maximumOrderSize;

    this.lastOrder = null;
  }

  public synchronized void receiveAndProcessTick(Tick tick) {
    if (tick.getTrades() == 0) {
      LOG.info("Tick contained no trades, skipping.");
      return;
    }
    timeSeries.addTick(tick);

    if (lastOrder == null) {
      lastOrder = new OrderState();
      LOG.info("First tick added, creating new OrderState object.");
    }

    int endIndex = timeSeries.getEndIndex();
    if (strategy.shouldEnter(endIndex)) {
      LOG.info("Strategy indicates BUY.");
      // TODO - Implement buy logic.
      if (lastOrder.type == OrderType.ASK || lastOrder.type == null) {
        executeBuy();
      } else {
        LOG.info("Strategy indicated BUY with BUY as last trade.");
      }
    } else if (strategy.shouldExit(endIndex)) {
      LOG.info("Strategy indicates SELL.");
      // TODO - Implement sell logic.
      if (lastOrder.type == OrderType.BID || lastOrder.type == null) {
        executeSell();
      } else {
        LOG.info("Strategy indicated SELL with SELL as last trade.");
      }
    } else {
      LOG.info("Strategy indicates WAIT.");
    }
  }

  /**
   * Buy the using the counter currency, typically USD.
   */
  private void executeBuy() {
    LOG.info("Executing buy order on {}", tradingApi.getMarketName());
    lastOrder.type = OrderType.BID;

    ListenableFuture<BigDecimal> counterBalance =
        tradingApi.getAvailableBalance(currencyPair.counter);
    ListenableFuture<String> tradeId = Futures.transformAsync(counterBalance, amount ->
        tradingApi.createMarketOrder(OrderType.BID, currencyPair, amount.min(maximumOrderSize)));
    tradeId = Futures.withTimeout(tradeId, 1000, TimeUnit.MILLISECONDS, executorService);

    Futures.addCallback(tradeId, new FutureCallback<String>() {
      public void onSuccess(String orderId) {
        LOG.info("Bought {} with order ID: {}", currencyPair.counter.toString(), orderId);
      }

      public void onFailure(Throwable t) {
        LOG.error("Failed to finish executing trade. Halting trading.", t);
        tradingApi.haltTrading();
      }
    });
  }

  /**
   * Sell using the base currency, typically BTC.
   */
  private void executeSell() {
    LOG.info("Executing sell order on {}", tradingApi.getMarketName());
    lastOrder.type = OrderType.ASK;

    ListenableFuture<BigDecimal> baseBalance = tradingApi.getAvailableBalance(currencyPair.base);
    ListenableFuture<String> tradeId = Futures.transformAsync(baseBalance, amount ->
        tradingApi.createMarketOrder(OrderType.ASK, currencyPair, amount.min(maximumOrderSize)));
    tradeId = Futures.withTimeout(tradeId, 1000, TimeUnit.MILLISECONDS, executorService);

    Futures.addCallback(tradeId, new FutureCallback<String>() {
      public void onSuccess(String orderId) {
        LOG.info("Sold {} with order ID: {}", currencyPair.base.toString(), orderId);
      }

      public void onFailure(Throwable t) {
        LOG.error("Failed to finish executing trade. Halting trading.", t);
        tradingApi.haltTrading();
      }
    });
  }

  /**
   * <p>
   * Models the state of an Order we have placed on the exchange.
   * </p>
   * <p>
   * Typically, you would maintain order state in a database or use some other persistent datasource
   * to recover from restarts and for audit purposes. In this example, we are storing the state in
   * memory to keep it simple.
   * </p>
   */
  private static class OrderState {

    /**
     * Id - default to null.
     */
    private String id = null;

    /**
     * Type: buy/sell. We default to null which means no order has been placed yet,
     * i.e. we've just started!
     */
    private OrderType type = null;

    /**
     * Price to buy/sell at - default to zero.
     */
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * Number of units to buy/sell - default to zero.
     */
    private BigDecimal amount = BigDecimal.ZERO;

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("type", type)
          .add("price", price)
          .add("amount", amount)
          .toString();
    }
  }
}
