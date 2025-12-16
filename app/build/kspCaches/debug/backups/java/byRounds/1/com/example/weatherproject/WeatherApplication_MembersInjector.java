package com.example.weatherproject;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
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
public final class WeatherApplication_MembersInjector implements MembersInjector<WeatherApplication> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public WeatherApplication_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<WeatherApplication> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new WeatherApplication_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(WeatherApplication instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.example.weatherproject.WeatherApplication.workerFactory")
  public static void injectWorkerFactory(WeatherApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
