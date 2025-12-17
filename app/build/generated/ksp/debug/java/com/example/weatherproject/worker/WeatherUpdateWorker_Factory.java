package com.example.weatherproject.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.weatherproject.data.repository.WeatherRepository;
import com.example.weatherproject.util.LocationProvider;
import com.example.weatherproject.util.PreferenceManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class WeatherUpdateWorker_Factory {
  private final Provider<WeatherRepository> weatherRepositoryProvider;

  private final Provider<PreferenceManager> preferenceManagerProvider;

  private final Provider<LocationProvider> locationProvider;

  public WeatherUpdateWorker_Factory(Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<PreferenceManager> preferenceManagerProvider,
      Provider<LocationProvider> locationProvider) {
    this.weatherRepositoryProvider = weatherRepositoryProvider;
    this.preferenceManagerProvider = preferenceManagerProvider;
    this.locationProvider = locationProvider;
  }

  public WeatherUpdateWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, weatherRepositoryProvider.get(), preferenceManagerProvider.get(), locationProvider.get());
  }

  public static WeatherUpdateWorker_Factory create(
      Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<PreferenceManager> preferenceManagerProvider,
      Provider<LocationProvider> locationProvider) {
    return new WeatherUpdateWorker_Factory(weatherRepositoryProvider, preferenceManagerProvider, locationProvider);
  }

  public static WeatherUpdateWorker newInstance(Context context, WorkerParameters workerParams,
      WeatherRepository weatherRepository, PreferenceManager preferenceManager,
      LocationProvider locationProvider) {
    return new WeatherUpdateWorker(context, workerParams, weatherRepository, preferenceManager, locationProvider);
  }
}
