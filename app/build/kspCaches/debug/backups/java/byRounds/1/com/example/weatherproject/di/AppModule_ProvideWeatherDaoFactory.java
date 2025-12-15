package com.example.weatherproject.di;

import com.example.weatherproject.data.local.AppDatabase;
import com.example.weatherproject.data.local.WeatherDao;
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
public final class AppModule_ProvideWeatherDaoFactory implements Factory<WeatherDao> {
  private final Provider<AppDatabase> appDatabaseProvider;

  public AppModule_ProvideWeatherDaoFactory(Provider<AppDatabase> appDatabaseProvider) {
    this.appDatabaseProvider = appDatabaseProvider;
  }

  @Override
  public WeatherDao get() {
    return provideWeatherDao(appDatabaseProvider.get());
  }

  public static AppModule_ProvideWeatherDaoFactory create(
      Provider<AppDatabase> appDatabaseProvider) {
    return new AppModule_ProvideWeatherDaoFactory(appDatabaseProvider);
  }

  public static WeatherDao provideWeatherDao(AppDatabase appDatabase) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWeatherDao(appDatabase));
  }
}
