package gy.jk.tick;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.inject.Inject;
import gy.jk.proto.Shared.Trade;
import gy.jk.tick.Annotations.TickLengthMillis;
import gy.jk.trade.Trader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ta4j.core.BaseTick;
import org.ta4j.core.Decimal;
import org.ta4j.core.Tick;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TickEngine {

  private static final Logger LOG = LogManager.getLogger();

  private final long tickLengthMillis;
  private final ListeningScheduledExecutorService executorService;
  private final Trader trader;

  private Tick currentTick;
  private ZonedDateTime lastTickEnd;

  @Inject
  TickEngine(@TickLengthMillis long tickLengthMillis,
      ListeningScheduledExecutorService executorService,
      Trader trader) {
    this.tickLengthMillis = tickLengthMillis;
    this.executorService = executorService;
    this.trader = trader;

    createNewTick();

    executorService.scheduleAtFixedRate(this::fireTick,
        tickLengthMillis, tickLengthMillis, TimeUnit.MILLISECONDS);

    LOG.info("Trader initialized.");
  }

  public synchronized void receiveTrade(Trade trade) {
    if (!trade.hasSize() || !trade.hasPrice() || !trade.hasExchange() || !trade.hasCurrencyPair()) {
      LOG.warn("Trade missing necessary information.\n{}", trade.toString());
      throw new IllegalArgumentException("Trade is missing necessary information.");
    }
    double volume = trade.getSize();
    double price = trade.getPrice();
    currentTick.addTrade(volume, price);
  }

  private synchronized void fireTick() {
    // If there are no trades within the tick, these will be null.
    // Use an Optional to prevent a NullPointerException.
    Optional<Decimal> closePrice = Optional.ofNullable(currentTick.getClosePrice());
    Optional<Decimal> volume = Optional.ofNullable(currentTick.getVolume());

    LOG.info("Last Tick Close: {} | Volume: {}",
        closePrice.map(Decimal::toString).orElse("NaN"), volume.orElse(Decimal.ZERO));
    Tick tick = currentTick;
    executorService.submit(() -> trader.receiveAndProcessTick(tick));
    createNewTick();
  }

  private void createNewTick() {
    if (lastTickEnd == null) {
      lastTickEnd = ZonedDateTime.now();
    }
    lastTickEnd = lastTickEnd.plus(tickLengthMillis, ChronoUnit.MILLIS);
    Duration duration = Duration.ofMillis(tickLengthMillis);
    currentTick = new BaseTick(duration, lastTickEnd);
  }

}
