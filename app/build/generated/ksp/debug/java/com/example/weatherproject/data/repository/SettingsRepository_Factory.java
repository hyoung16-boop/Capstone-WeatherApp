package com.example.weatherproject.data.repository;

import com.example.weatherproject.util.PreferenceManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SettingsRepository_Factory implements Factory<SettingsRepository> {
  private final Provider<PreferenceManager> prefManagerProvider;

  public SettingsRepository_Factory(Provider<PreferenceManager> prefManagerProvider) {
    this.prefManagerProvider = prefManagerProvider;
  }

  @Override
  public SettingsRepository get() {
    return newInstance(prefManagerProvider.get());
  }

  public static SettingsRepository_Factory create(Provider<PreferenceManager> prefManagerProvider) {
    return new SettingsRepository_Factory(prefManagerProvider);
  }

  public static SettingsRepository newInstance(PreferenceManager prefManager) {
    return new SettingsRepository(prefManager);
  }
}
