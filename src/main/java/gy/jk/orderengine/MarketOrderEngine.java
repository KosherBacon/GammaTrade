package gy.jk.orderengine;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import gy.jk.exchange.TradingApi;
import gy.jk.trade.Annotations.MaximumOrderSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author Joshua Kahn
 */
@Singleton
public class MarketOrderEngine extends OrderEngine {

  private static final Logger LOG = LogManager.getLogger();

  private final ListeningExecutorService executorService;
  private final TradingApi tradingApi;
  private final CurrencyPair currencyPair;
  private final BigDecimal maximumOrderSize;
  private final OrderState lastOrder;

  @Inject
  MarketOrderEngine(ListeningExecutorService executorService,
      TradingApi tradingApi,
      CurrencyPair currencyPair,
      @MaximumOrderSize BigDecimal maximumOrderSize,
      LastOrderEngine lastOrderEngine) {
    this.executorService = executorService;
    this.tradingApi = tradingApi;
    this.currencyPair = currencyPair;
    this.maximumOrderSize = maximumOrderSize;

    lastOrder = new OrderState();
    try {
      lastOrder.type = lastOrderEngine.getLastTrade().get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Override
  public ListenableFuture<Optional<String>> placeOrder(OrderType orderType) {
    if (orderType == OrderType.BID && lastOrder.type == OrderType.ASK) {
      LOG.info("Executing buy order on {}", tradingApi.getMarketName());
      return executeBuy(orderType);
    } else if (orderType == OrderType.ASK && lastOrder.type == OrderType.BID) {
      LOG.info("Executing sell order on {}", tradingApi.getMarketName());
      return executeSell(orderType);
    } else {
      return Futures.immediateFuture(Optional.of("no-trade-necessary"));
    }
  }

  private ListenableFuture<Optional<String>> executeBuy(OrderType orderType) {
    ListenableFuture<BigDecimal> counterBalance =
        tradingApi.getAvailableBalance(currencyPair.counter);
    return Futures.transformAsync(counterBalance, amount -> {
      BigDecimal toBuy = Objects.requireNonNull(amount, "Amount was null!")
          .min(maximumOrderSize);
      ListenableFuture<String> marketOrder =
          tradingApi.createMarketOrder(orderType, currencyPair, toBuy);
      return Futures.transformAsync(marketOrder, order ->
          Futures.immediateFuture(Optional.of(Objects.requireNonNull(order, "OrderId was null!"))),
          executorService);
    }, executorService);
  }

  private ListenableFuture<Optional<String>> executeSell(OrderType orderType) {
    ListenableFuture<BigDecimal> baseBalance =
        tradingApi.getAvailableBalance(currencyPair.base);
    return Futures.transformAsync(baseBalance, amount -> {
      BigDecimal toSell = Objects.requireNonNull(amount, "Amount was null!");
      ListenableFuture<String> marketOrder =
          tradingApi.createMarketOrder(orderType, currencyPair, toSell);
      return Futures.transformAsync(marketOrder, order ->
              Futures.immediateFuture(Optional.of(Objects.requireNonNull(order, "OrderId was null!"))),
          executorService);
    }, executorService);
  }

}
