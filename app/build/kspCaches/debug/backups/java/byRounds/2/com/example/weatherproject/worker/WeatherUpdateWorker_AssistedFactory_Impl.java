package com.example.weatherproject.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class WeatherUpdateWorker_AssistedFactory_Impl implements WeatherUpdateWorker_AssistedFactory {
  private final WeatherUpdateWorker_Factory delegateFactory;

  WeatherUpdateWorker_AssistedFactory_Impl(WeatherUpdateWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public WeatherUpdateWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<WeatherUpdateWorker_AssistedFactory> create(
      WeatherUpdateWorker_Factory delegateFactory) {
    return InstanceFactory.create(new WeatherUpdateWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<WeatherUpdateWorker_AssistedFactory> createFactoryProvider(
      WeatherUpdateWorker_Factory delegateFactory) {
    return InstanceFactory.create(new WeatherUpdateWorker_AssistedFactory_Impl(delegateFactory));
  }
}
