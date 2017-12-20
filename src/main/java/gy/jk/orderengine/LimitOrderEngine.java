package gy.jk.exchange;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.inject.Inject;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;

import java.util.List;
import java.util.Objects;

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
public class LimitOrderEngine {

  private final ListeningScheduledExecutorService executorService;
  private final TradingApi tradingApi;
  private final CurrencyPair currencyPair;

  private final OrderState lastOrder;

  @Inject
  LimitOrderEngine(ListeningScheduledExecutorService executorService,
      TradingApi tradingApi,
      CurrencyPair currencyPair) {
    this.executorService = executorService;
    this.tradingApi = tradingApi;
    this.currencyPair = currencyPair;

    this.lastOrder = new OrderState();
  }

  public void placeOrder(OrderType orderType) {
    // Check if there are any existing orders.
    ListenableFuture<OpenOrders> openOrders = tradingApi.getOpenOrders(currencyPair);
    Futures.transformAsync(openOrders, orders -> {
      List<LimitOrder> limitOrders =
          Objects.requireNonNull(orders, "LimitOrder list was null!").getOpenOrders();
      boolean orderTypeMatchesExisting = limitOrders.stream()
          .anyMatch(order -> order.getType() == orderType);

      // Our old order matches the incoming order type.
      // Cancel the old order, and place a new one at the current best limit price.
      if (orderTypeMatchesExisting) {

      }
      return null;
    });
  }

  private void checkOrderStatus() {
  }

  private void cancelOrders() {
  }

  private final static class OrderState {

    private OrderType last = null;

    private String id = null;

  }

}
