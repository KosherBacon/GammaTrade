package jk.gy.orderengine;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import gy.jk.exchange.TradingApi;
import gy.jk.orderengine.LimitOrderEngine;
import jk.gy.TestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LimitOrderEngineTest {

  private static final CurrencyPair currencyPair = CurrencyPair.BTC_USD;

  @Mock private TradingApi tradingApi;

  private OpenOrders openOrders;
  private LimitOrderEngine limitOrderEngine;

  @Test
  public void testNoConflictingOrdersBuyNotLive() throws Exception {
    openOrders = new OpenOrders(Collections.emptyList());
    limitOrderEngine = new LimitOrderEngine(TestHelper.TEST_EXECUTOR_SERVICE, tradingApi,
        currencyPair, false);

    ListenableFuture<BigDecimal> price = Futures.immediateFuture(BigDecimal.ONE);
    ListenableFuture<BigDecimal> balance = Futures.immediateFuture(BigDecimal.TEN);
    ListenableFuture<Optional<String>> orderId =
        Futures.immediateFuture(Optional.of("not-live-trade-id"));

    when(tradingApi.getOpenOrders(eq(currencyPair)))
        .thenReturn(Futures.immediateFuture(openOrders));
    when(tradingApi.getBestPriceFromOrderBook(OrderType.BID, currencyPair))
        .thenReturn(price);
    when(tradingApi.getAvailableBalance(currencyPair.counter))
        .thenReturn(balance);

    assertThat(limitOrderEngine.placeOrder(OrderType.BID).get())
        .isEqualTo(orderId.get());
  }

}
