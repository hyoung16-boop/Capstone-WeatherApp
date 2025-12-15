package com.example.weatherproject.di;

import com.example.weatherproject.data.local.WeatherDao;
import com.example.weatherproject.data.repository.WeatherRepository;
import com.example.weatherproject.network.WeatherApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AppModule_ProvideWeatherRepositoryFactory implements Factory<WeatherRepository> {
  private final Provider<WeatherApiService> weatherApiProvider;

  private final Provider<WeatherDao> weatherDaoProvider;

  public AppModule_ProvideWeatherRepositoryFactory(Provider<WeatherApiService> weatherApiProvider,
      Provider<WeatherDao> weatherDaoProvider) {
    this.weatherApiProvider = weatherApiProvider;
    this.weatherDaoProvider = weatherDaoProvider;
  }

  @Override
  public WeatherRepository get() {
    return provideWeatherRepository(weatherApiProvider.get(), weatherDaoProvider.get());
  }

  public static AppModule_ProvideWeatherRepositoryFactory create(
      Provider<WeatherApiService> weatherApiProvider, Provider<WeatherDao> weatherDaoProvider) {
    return new AppModule_ProvideWeatherRepositoryFactory(weatherApiProvider, weatherDaoProvider);
  }

  public static WeatherRepository provideWeatherRepository(WeatherApiService weatherApi,
      WeatherDao weatherDao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWeatherRepository(weatherApi, weatherDao));
  }
}
