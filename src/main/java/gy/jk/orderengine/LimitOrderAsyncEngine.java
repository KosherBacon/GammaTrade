package gy.jk.orderengine;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import gy.jk.exchange.Annotations.LiveTrading;
import gy.jk.exchange.TradingApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Joshua Kahn
 *
 * Possible states:
 * - No orders
 *
 * - Last order was BID, didn't fully fill
 *   - Current order is BID
 *   - Current order is ASK
 *
 * - Last order was ASK, didn't fully fill
 *   - Current order is ASK
 *   - Current order is BID
 *
 * We maintain the invariant that there will
 * always be at most one order in the exchange
 * at any time.
 */
public class LimitOrderAsyncEngine extends OrderEngine {

  private static final Logger LOG = LogManager.getLogger();

  private final ListeningExecutorService executorService;
  private final TradingApi tradingApi;
  private final CurrencyPair currencyPair;
  private final boolean liveTrading;

  @Inject
  public LimitOrderAsyncEngine(ListeningExecutorService executorService,
      TradingApi tradingApi,
      CurrencyPair currencyPair,
      @LiveTrading boolean liveTrading) {
    this.executorService = executorService;
    this.tradingApi = tradingApi;
    this.currencyPair = currencyPair;
    this.liveTrading = liveTrading;
  }

  @Override
  public ListenableFuture<Optional<String>> placeOrder(OrderType orderType) {
    if (orderType == OrderType.BID) {
      LOG.info("Executing buy order on {}", tradingApi.getMarketName());
    } else {
      LOG.info("Executing sell order on {}", tradingApi.getMarketName());
    }

    // Check if there are any existing orders.
    ListenableFuture<OpenOrders> openOrders = tradingApi.getOpenOrders(currencyPair);
    Currency currency = orderType == OrderType.BID ? currencyPair.counter : currencyPair.base;

    return Futures.transformAsync(openOrders, orders -> {
      List<LimitOrder> limitOrders =
          Objects.requireNonNull(orders, "LimitOrder list was null!").getOpenOrders();

      boolean orderTypeMatchesExisting = limitOrders.stream()
          .anyMatch(order -> order.getType() == orderType);
      // If orderTypeDifferentThanExisting is true then we should cancel our outstanding order.
      boolean orderTypeDifferentThanExisting = limitOrders.stream()
          .anyMatch(order -> order.getType() != orderType);

      // Our old order matches the incoming order type.
      // Cancel the old order, and place a new one at the current best limit price.
      if (orderTypeMatchesExisting) {
        LOG.info("Remnant order, same order type.");

        // True if the order was cancelled, false if it already filled.
        ListenableFuture<Boolean> canceled = tradingApi.cancelAllOrders();
        return Futures.transformAsync(canceled, wasCanceled -> {
          // If this was true, then we can continue.
          // If this was false, then do nothing as our order filled and orderType matched the
          // incoming request.
          if (Objects.requireNonNull(wasCanceled, "Failed to cancel orders!")) {
            LOG.info("Successfully canceled orders.");

            // Put in a new order at the current best limit order price.
            ListenableFuture<BigDecimal> price =
                tradingApi.getBestPriceFromOrderBook(orderType, currencyPair);
            ListenableFuture<BigDecimal> amount = getAmountAtBestPrice(currency, price);
            ListenableFuture<List<BigDecimal>> priceAmountData = Futures.allAsList(price, amount);
            return Futures.transformAsync(priceAmountData, values -> {
              Objects.requireNonNull(values, "priceAmountData was null!");
              BigDecimal priceVal = Objects.requireNonNull(values.get(0), "Price was null!");
              BigDecimal amountVal = Objects.requireNonNull(values.get(1), "Amount was null!");
              if (!liveTrading) {
                return Futures.immediateFuture(Optional.of("not-live-trade-id"));
              }
              ListenableFuture<String> order = tradingApi.createLimitOrder(orderType, currencyPair,
                  amountVal, priceVal);
              return Futures.transformAsync(order, orderStr -> {
                Objects.requireNonNull(orderStr, "orderStr was null!");
                return Futures.immediateFuture(Optional.of(orderStr));
              });
            }, executorService);
          }
          return Futures.immediateFuture(Optional.empty());
        }, executorService);
      } else if (orderTypeDifferentThanExisting) {
        LOG.info("Remnant order different than existing order.");

        // Cancel any outstanding order.
        ListenableFuture<Boolean> cancelOrderStatus = tradingApi.cancelAllOrders();
        return Futures.transformAsync(cancelOrderStatus, orderStatus -> {
          // Put in a new order at the current best limit order price.
          ListenableFuture<BigDecimal> price =
              tradingApi.getBestPriceFromOrderBook(orderType, currencyPair);
          ListenableFuture<BigDecimal> amount = getAmountAtBestPrice(currency, price);
          ListenableFuture<List<BigDecimal>> priceAmountData = Futures.allAsList(price, amount);
          return Futures.transformAsync(priceAmountData, values -> {
            Objects.requireNonNull(values, "priceAmountData was null!");
            BigDecimal priceVal = Objects.requireNonNull(values.get(0), "Price was null!");
            BigDecimal amountVal = Objects.requireNonNull(values.get(1), "Amount was null!");
            if (amountVal.round(CURRENCY_CONTEXTS.get(currency))
                .compareTo(CURRENCY_MIN_ORDERS.get(currency)) < 0) {
              return Futures.immediateFuture(Optional.empty());
            }
            if (!liveTrading) {
              return Futures.immediateFuture(Optional.of("not-live-trade-id"));
            }
            ListenableFuture<String> order = tradingApi.createLimitOrder(orderType, currencyPair,
                amountVal, priceVal);
            return Futures.transformAsync(order, orderStr -> {
              Objects.requireNonNull(orderStr, "orderStr was null!");
              return Futures.immediateFuture(Optional.of(orderStr));
            }, executorService);
          }, executorService);
        }, executorService);
      } else {
        // Put in a new order at the current best limit order price.
        ListenableFuture<BigDecimal> price =
            tradingApi.getBestPriceFromOrderBook(orderType, currencyPair);
        ListenableFuture<BigDecimal> amount = getAmountAtBestPrice(currency, price);
        ListenableFuture<List<BigDecimal>> priceAmountData = Futures.allAsList(price, amount);
        return Futures.transformAsync(priceAmountData, values -> {
          Objects.requireNonNull(values, "priceAmountData was null!");
          BigDecimal priceVal = Objects.requireNonNull(values.get(0), "Price was null!");
          BigDecimal amountVal = Objects.requireNonNull(values.get(1), "Amount was null!");
          if (amountVal.round(CURRENCY_CONTEXTS.get(currency))
              .compareTo(BigDecimal.valueOf(0.0001)) < 0) {
            return Futures.immediateFuture(Optional.empty());
          }
          if (!liveTrading) {
            return Futures.immediateFuture(Optional.of("not-live-trade-id"));
          }
          ListenableFuture<String> order = tradingApi.createLimitOrder(orderType, currencyPair,
              amountVal, priceVal);
          return Futures.transformAsync(order, orderStr -> {
            Objects.requireNonNull(orderStr, "orderStr was null!");
            return Futures.immediateFuture(Optional.of(orderStr));
          }, executorService);
        }, executorService);
      }
    }, executorService);
  }

  public ListenableFuture<BigDecimal> getAmountAtBestPrice(Currency currency,
      ListenableFuture<BigDecimal> price) {

    ListenableFuture<BigDecimal> balance =
        tradingApi.getAvailableBalance(currency);
    ListenableFuture<List<BigDecimal>> priceData = Futures.allAsList(price, balance);
    return Futures.transformAsync(priceData, values -> {
      Objects.requireNonNull(values, "Values list was null!");
      BigDecimal balanceVal = Objects.requireNonNull(values.get(1), "Balance was null!");
      BigDecimal priceVal = Objects.requireNonNull(values.get(0), "Price was null!");
      BigDecimal amountToBuy = balanceVal.divide(priceVal, CURRENCY_CONTEXTS.get(currency));
      return Futures.immediateFuture(amountToBuy);
    }, executorService);
  }
}
