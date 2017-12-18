package gy.jk.backtest;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.inject.Inject;
import com.opencsv.CSVReader;
import gy.jk.tick.Annotations.TickLengthMillis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseTick;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Joshua Kahn
 */
public class BacktestDataLoader {

  private static final Logger LOG = LoggerFactory.getLogger(BacktestDataLoader.class);

  private final ListeningScheduledExecutorService executorService;
  private final long tickLengthMillis;

  @Inject
  BacktestDataLoader(ListeningScheduledExecutorService executorService,
      @TickLengthMillis long tickLengthMillis) {
    this.executorService = executorService;
    this.tickLengthMillis = tickLengthMillis;
  }

  public ListenableFuture<TimeSeries> getKrakenBTCUSD() {
    return executorService.submit(() -> {
      Instant start = Instant.now();
      LOG.info("Loading backtest data from krakenUSD.csv.");

      InputStream stream =
          BacktestDataLoader.class.getResourceAsStream("/data/krakenUSD3.csv");
      CSVReader csvReader = null;
      List<String[]> lines = new ArrayList<>();

      try {
        csvReader = new CSVReader(
            new InputStreamReader(stream, Charset.forName("UTF-8")),',');
        lines.addAll(csvReader.readAll());
        lines.remove(0);
      } finally {
        if (csvReader != null) {
          try {
            csvReader.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      List<Tick> ticks = new ArrayList<>();
      if (!lines.isEmpty()) {
        ZonedDateTime beginTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(
                Long.parseLong(lines.get(0)[0]) * 1000), ZoneId.systemDefault());
        ZonedDateTime endTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(
                Long.parseLong(lines.get(lines.size() - 1)[0]) * 1000), ZoneId.systemDefault());
        if (beginTime.isAfter(endTime)) {
          Instant beginInstant = beginTime.toInstant();
          Instant endInstant = endTime.toInstant();
          beginTime = ZonedDateTime.ofInstant(endInstant, ZoneId.systemDefault());
          endTime = ZonedDateTime.ofInstant(beginInstant, ZoneId.systemDefault());
          // Since the CSV file has the most recent trades at the top of the file,
          // we'll reverse the list to feed the List<Tick> correctly.
          Collections.reverse(lines);
        }

        ticks = buildEmptyTicks(beginTime, endTime, (int) tickLengthMillis / 1000);
        // Filling the ticks with trades
        for (String[] tradeLine : lines) {
          ZonedDateTime tradeTimestamp = ZonedDateTime.ofInstant(
              Instant.ofEpochMilli(Long.parseLong(tradeLine[0]) * 1000), ZoneId.systemDefault());

          int index = (int) ((tradeTimestamp.toEpochSecond() - beginTime.toEpochSecond())
              / (tickLengthMillis / 1000));
          double tradePrice = Double.parseDouble(tradeLine[1]);
          double tradeAmount = Double.parseDouble(tradeLine[2]);
          ticks.get(index).addTrade(tradeAmount, tradePrice);

          // The below is the O(n^2) way. Don't do it.
//          ticks.stream()
//              .filter(tick -> tick.inPeriod(tradeTimestamp))
//              .findFirst()
//              .ifPresent(tick -> {
//                double tradePrice = Double.parseDouble(tradeLine[1]);
//                double tradeAmount = Double.parseDouble(tradeLine[2]);
//                tick.addTrade(tradeAmount, tradePrice);
//              });
//          for (Tick tick : ticks) {
//            if (tick.inPeriod(tradeTimestamp)) {
//              double tradePrice = Double.parseDouble(tradeLine[1]);
//              double tradeAmount = Double.parseDouble(tradeLine[2]);
//              tick.addTrade(tradeAmount, tradePrice);
//            }
//          }
        }
        // Removing still empty ticks
        removeEmptyTicks(ticks);
      }

      if (ticks.isEmpty()) {
        throw new IllegalStateException("Cannot continue, no ticks.");
      }

      Instant end = Instant.now();
      LOG.info("Finished loading krakenUSD.csv, took {} seconds.",
          Duration.between(start, end).toMillis() / 1000L);

      return new BaseTimeSeries(ticks);
    });
  }

  private static List<Tick> buildEmptyTicks(ZonedDateTime beginTime, ZonedDateTime endTime,
      int duration) {

    List<Tick> emptyTicks = new ArrayList<>();

    Duration tickDuration = Duration.ofSeconds(duration);
    ZonedDateTime tickEndTime = beginTime;
    do {
      tickEndTime = tickEndTime.plus(tickDuration);
      emptyTicks.add(new BaseTick(tickDuration, tickEndTime));
    } while (!tickEndTime.isAfter(endTime));

    return emptyTicks;
  }

  private static void removeEmptyTicks(List<Tick> ticks) {
    ticks.removeIf(tick -> tick.getTrades() == 0);
  }

}
