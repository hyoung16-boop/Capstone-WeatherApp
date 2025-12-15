package com.example.weatherproject.di;

import android.content.Context;
import com.example.weatherproject.util.PreferenceManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvidePreferenceManagerFactory implements Factory<PreferenceManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvidePreferenceManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PreferenceManager get() {
    return providePreferenceManager(contextProvider.get());
  }

  public static AppModule_ProvidePreferenceManagerFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvidePreferenceManagerFactory(contextProvider);
  }

  public static PreferenceManager providePreferenceManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePreferenceManager(context));
  }
}
