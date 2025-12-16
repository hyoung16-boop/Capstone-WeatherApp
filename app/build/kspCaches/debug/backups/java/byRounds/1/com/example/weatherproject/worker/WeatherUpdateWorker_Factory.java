package com.example.weatherproject.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.weatherproject.data.repository.WeatherRepository;
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

  public WeatherUpdateWorker_Factory(Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<PreferenceManager> preferenceManagerProvider) {
    this.weatherRepositoryProvider = weatherRepositoryProvider;
    this.preferenceManagerProvider = preferenceManagerProvider;
  }

  public WeatherUpdateWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, weatherRepositoryProvider.get(), preferenceManagerProvider.get());
  }

  public static WeatherUpdateWorker_Factory create(
      Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<PreferenceManager> preferenceManagerProvider) {
    return new WeatherUpdateWorker_Factory(weatherRepositoryProvider, preferenceManagerProvider);
  }

  public static WeatherUpdateWorker newInstance(Context context, WorkerParameters workerParams,
      WeatherRepository weatherRepository, PreferenceManager preferenceManager) {
    return new WeatherUpdateWorker(context, workerParams, weatherRepository, preferenceManager);
  }
}
