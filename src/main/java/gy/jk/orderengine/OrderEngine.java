package gy.jk.orderengine;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.Order;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

public abstract class OrderEngine {

  static final ImmutableMap<Currency, MathContext> CURRENCY_CONTEXTS =
      new ImmutableMap.Builder<Currency, MathContext>()
          .put(Currency.USD, new MathContext(2, RoundingMode.DOWN))
          .put(Currency.BTC, new MathContext(8, RoundingMode.DOWN))
          .put(Currency.ETH, new MathContext(8, RoundingMode.DOWN))
          .build();

  static final ImmutableMap<Currency, BigDecimal> CURRENCY_MIN_ORDERS =
      new ImmutableMap.Builder<Currency, BigDecimal>()
          .put(Currency.BTC, BigDecimal.valueOf(1, -4))
          .put(Currency.ETH, BigDecimal.valueOf(1, -3))
      .build();

  public abstract ListenableFuture<Optional<String>> placeOrder(Order.OrderType orderType);

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
   static class OrderState {

    /**
     * Id - default to null.
     */
    private String id = null;

    /**
     * Type: buy/sell. We default to null which means no order has been placed yet,
     * i.e. we've just started!
     */
    private Order.OrderType type = null;

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
