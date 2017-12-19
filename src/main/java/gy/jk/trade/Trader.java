package gy.jk.trade;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.inject.Inject;
import gy.jk.exchange.TradingApi;
import gy.jk.strategy.StrategyBuilder;
import gy.jk.trade.Annotations.MaximumOrderSize;
import gy.jk.trade.Annotations.TradeStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.ta4j.core.Decimal;
import org.ta4j.core.Strategy;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Joshua Kahn
 */
public class Trader {

  private static final Logger LOG = LogManager.getLogger();

  // Map a currency to the amount of precision we have in the exchange (GDAX for now).
  private static final ImmutableMap<Currency, MathContext> CURRENCY_CONTEXTS =
      new ImmutableMap.Builder<Currency, MathContext>()
          .put(Currency.USD, new MathContext(2, RoundingMode.DOWN))
          .put(Currency.BTC, new MathContext(8, RoundingMode.DOWN))
          .put(Currency.ETH, new MathContext(8, RoundingMode.DOWN))
          .build();

  private static final MathContext MATH_CONTEXT = Decimal.MATH_CONTEXT;
  private static final BigDecimal COUNTER_KEEP = new BigDecimal("0.95", MATH_CONTEXT);

  private final Strategy strategy;
  private final TimeSeries timeSeries;
  private final TradingApi tradingApi;
  private final CurrencyPair currencyPair;
  private final ListeningScheduledExecutorService executorService;
  private final BigDecimal maximumOrderSize;

  private OrderState lastOrder;
  private BigDecimal tickClose;

  @Inject
  Trader(@TradeStrategy StrategyBuilder strategyBuilder,
      TimeSeries timeSeries,
      TradingApi tradingApi,
      CurrencyPair currencyPair,
      ListeningScheduledExecutorService executorService,
      @MaximumOrderSize BigDecimal maximumOrderSize) {
    this.strategy = strategyBuilder.buildStrategy(timeSeries);

    this.timeSeries = timeSeries;
    this.tradingApi = tradingApi;
    this.currencyPair = currencyPair;
    this.executorService = executorService;
    this.maximumOrderSize = maximumOrderSize;

    this.lastOrder = null;
    this.tickClose = null;

    LOG.info("Using strategy: {}", strategyBuilder.getClass().getSimpleName());

    try {
      BigDecimal usdBalance = tradingApi.getAvailableBalance(Currency.USD).get();
      BigDecimal btcBalance = tradingApi.getAvailableBalance(Currency.BTC).get();
      lastOrder = new OrderState();
      if (usdBalance.compareTo(btcBalance) < 0) {
        lastOrder.type = OrderType.BID; // BID = Buy
        LOG.info("Last order was BUY!");
      } else {
        lastOrder.type = OrderType.ASK; // ASK = Sell
        LOG.info("Last order was SELL!");
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
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

    // Get the close price of the tick.
    tickClose = new BigDecimal(tick.getClosePrice().toDouble(), MATH_CONTEXT);

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
    LOG.info("Executing buy order on {}", tradingApi.getMarketName());
    lastOrder.type = OrderType.BID;

    try {
      List<LimitOrder> openOrders = tradingApi.getOpenOrders(currencyPair).get().getOpenOrders();

      // If there are any remaining ASK orders we should cancel it and return.
      if (openOrders.stream()
          .anyMatch(order -> order.getType() == OrderType.ASK)) {
        tradingApi.cancelAllOrders().get();
        return;
      } else if (openOrders.stream().anyMatch(order -> order.getType() == OrderType.BID)) {
        // May have been outbid and should cancel and update.
        tradingApi.cancelAllOrders().get();
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    // Get the amount of available USD in the account.
    ListenableFuture<BigDecimal> counterBalance =
        tradingApi.getAvailableBalance(currencyPair.counter);

    ListenableFuture<BigDecimal> bestPrice = tradingApi.getBestPriceFromOrderBook(OrderType.BID,
        currencyPair);
    ListenableFuture<List<BigDecimal>> data = Futures.allAsList(counterBalance, bestPrice);
    ListenableFuture<BigDecimal> amountToBuy = Futures.transformAsync(data, values -> {
      if (values == null) {
        throw new NullPointerException("values was null");
      }
      return Futures.immediateFuture(values.get(0)
          .divide(values.get(1), CURRENCY_CONTEXTS.get(currencyPair.base)));
    });
    data = Futures.allAsList(bestPrice, amountToBuy);
    ListenableFuture<String> tradeId = Futures.transformAsync(data, values -> {
      if (values == null) {
        throw new NullPointerException("values was null");
      }
      BigDecimal price = values.get(0);
      BigDecimal amount = values.get(1);
      return tradingApi.createLimitOrder(OrderType.BID, currencyPair, amount, price);
    });

//    ListenableFuture<String> tradeId = Futures.transformAsync(counterBalance, amount -> {
//      if (amount == null) {
//        throw new NullPointerException("amount was null");
//      }
//      BigDecimal toBuy = amount.multiply(COUNTER_KEEP, CURRENCY_CONTEXTS.get(currencyPair.counter))
//          .min(tickClose);
//      LOG.info("Buying {} {} of {}", amount, currencyPair.counter.toString(),
//          currencyPair.base.toString());
//      return tradingApi.createMarketOrder(OrderType.BID, currencyPair, toBuy);
//    });
//    tradeId = Futures.withTimeout(tradeId, 1000, TimeUnit.MILLISECONDS, executorService);

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

    try {
      List<LimitOrder> openOrders = tradingApi.getOpenOrders(currencyPair).get().getOpenOrders();

      // If there are any remaining BID orders we should cancel it and return.
      if (openOrders.stream()
          .anyMatch(order -> order.getType() == OrderType.BID)) {
        tradingApi.cancelAllOrders().get();
        return;
      } else if (openOrders.stream().anyMatch(order -> order.getType() == OrderType.ASK)) {
        // May have been outbid and should cancel and update.
        tradingApi.cancelAllOrders().get();
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    ListenableFuture<BigDecimal> baseBalance = tradingApi.getAvailableBalance(currencyPair.base);

    ListenableFuture<BigDecimal> bestPrice = tradingApi.getBestPriceFromOrderBook(OrderType.ASK,
        currencyPair);
    ListenableFuture<List<BigDecimal>> data = Futures.allAsList(baseBalance, bestPrice);
    ListenableFuture<BigDecimal> amountToBuy = Futures.transformAsync(data, values -> {
      if (values == null) {
        throw new NullPointerException("values was null");
      }
      return Futures.immediateFuture(values.get(0)
          .divide(values.get(1), CURRENCY_CONTEXTS.get(currencyPair.base)));
    });
    data = Futures.allAsList(bestPrice, amountToBuy);
    ListenableFuture<String> tradeId = Futures.transformAsync(data, values -> {
      if (values == null) {
        throw new NullPointerException("values was null");
      }
      BigDecimal price = values.get(0);
      BigDecimal amount = values.get(1);
      return tradingApi.createLimitOrder(OrderType.ASK, currencyPair, amount, price);
    });

//    ListenableFuture<String> tradeId = Futures.transformAsync(baseBalance, amount -> {
//      if (amount == null) {
//        throw new NullPointerException("amount was null");
//      }
//      BigDecimal toSell = amount.min(maximumOrderSize)
//          .round(CURRENCY_CONTEXTS.get(currencyPair.base));
//      LOG.info("Selling {} {}", amount, currencyPair.base.toString());
//      return tradingApi.createMarketOrder(OrderType.ASK, currencyPair, toSell);
//    });
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
