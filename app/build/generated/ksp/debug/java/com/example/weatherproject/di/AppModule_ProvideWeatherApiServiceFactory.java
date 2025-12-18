package com.example.weatherproject.di;

import com.example.weatherproject.network.WeatherApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
  private final Provider<Retrofit> retrofitProvider;

  public AppModule_ProvideWeatherApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public WeatherApiService get() {
    return provideWeatherApiService(retrofitProvider.get());
  }

  public static AppModule_ProvideWeatherApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideWeatherApiServiceFactory(retrofitProvider);
  }

  public static WeatherApiService provideWeatherApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWeatherApiService(retrofit));
  }
}
