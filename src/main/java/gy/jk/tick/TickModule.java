package gy.jk.tick;

import com.google.inject.AbstractModule;
import gy.jk.tick.Annotations.TickLengthMillis;

public class TickModule extends AbstractModule {

  private static final long TICK_LENGTH_MILLIS = 10 * 1000; // 10 seconds per tick

  @Override
  protected void configure() {
    bindConstant().annotatedWith(TickLengthMillis.class).to(TICK_LENGTH_MILLIS);
  }
}
