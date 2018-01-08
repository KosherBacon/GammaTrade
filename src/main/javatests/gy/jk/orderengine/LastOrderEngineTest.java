package jk.gy.orderengine;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import gy.jk.exchange.TradingApi;
import gy.jk.orderengine.LastOrderEngine;
import jk.gy.TestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LastOrderEngineTest {

  private static final CurrencyPair CURRENCY_PAIR = CurrencyPair.ETH_USD;

  @Mock private TradingApi tradingApi;

  private LastOrderEngine lastOrderEngine;

  @Before
  public void setUp() {
    lastOrderEngine = new LastOrderEngine(tradingApi, CURRENCY_PAIR, TestHelper.TEST_EXECUTOR_SERVICE);
  }

  @Test
  public void testLastOrderBuy() throws Exception {
    ListenableFuture<BigDecimal> baseBalance = Futures.immediateFuture(BigDecimal.ONE);
    ListenableFuture<BigDecimal> counterBalance = Futures.immediateFuture(BigDecimal.ZERO);

    when(tradingApi.getAvailableBalance(CURRENCY_PAIR.base))
        .thenReturn(baseBalance);
    when(tradingApi.getAvailableBalance(CURRENCY_PAIR.counter))
        .thenReturn(counterBalance);

    assertThat(lastOrderEngine.getLastTrade().get())
        .isEqualTo(OrderType.BID);
  }

  @Test
  public void testLastOrderSell() throws Exception {
    ListenableFuture<BigDecimal> baseBalance = Futures.immediateFuture(BigDecimal.ZERO);
    ListenableFuture<BigDecimal> counterBalance = Futures.immediateFuture(BigDecimal.ONE);

    when(tradingApi.getAvailableBalance(CURRENCY_PAIR.base))
        .thenReturn(baseBalance);
    when(tradingApi.getAvailableBalance(CURRENCY_PAIR.counter))
        .thenReturn(counterBalance);

    assertThat(lastOrderEngine.getLastTrade().get())
        .isEqualTo(OrderType.ASK);
  }

}
