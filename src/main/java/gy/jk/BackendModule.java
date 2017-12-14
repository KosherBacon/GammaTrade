package gy.jk;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BackendModule extends AbstractModule {

  private static final EventBus BACKEND_EVENT_BUS = new EventBus("Backend_Event_Bus");

  private static final int NUM_THREADS = 16;
  private static final ScheduledExecutorService TRADER_EXECUTOR_SERVICE =
      Executors.newScheduledThreadPool(NUM_THREADS);
  private static final ListeningExecutorService LISTENING_EXECUTOR_SERVICE =
      MoreExecutors.listeningDecorator(TRADER_EXECUTOR_SERVICE);

  @Override
  protected void configure() {
    bind(ScheduledExecutorService.class).toInstance(TRADER_EXECUTOR_SERVICE);
    bind(ListeningExecutorService.class).toInstance(LISTENING_EXECUTOR_SERVICE);

    bind(EventBus.class).toInstance(BACKEND_EVENT_BUS);
    bindListener(Matchers.any(), new TypeListener() {
      public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
        typeEncounter.register((InjectionListener<I>) BACKEND_EVENT_BUS::register);
      }
    });
  }
}
