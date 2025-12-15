package com.example.weatherproject.di;

import com.example.weatherproject.network.WeatherApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideWeatherApiServiceFactory implements Factory<WeatherApiService> {
  @Override
  public WeatherApiService get() {
    return provideWeatherApiService();
  }

  public static AppModule_ProvideWeatherApiServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WeatherApiService provideWeatherApiService() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWeatherApiService());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideWeatherApiServiceFactory INSTANCE = new AppModule_ProvideWeatherApiServiceFactory();
  }
}
