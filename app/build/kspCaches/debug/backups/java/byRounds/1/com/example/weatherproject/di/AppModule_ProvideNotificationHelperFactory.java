package com.example.weatherproject.di;

import com.example.weatherproject.util.NotificationHelper;
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
public final class AppModule_ProvideNotificationHelperFactory implements Factory<NotificationHelper> {
  @Override
  public NotificationHelper get() {
    return provideNotificationHelper();
  }

  public static AppModule_ProvideNotificationHelperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NotificationHelper provideNotificationHelper() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideNotificationHelper());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideNotificationHelperFactory INSTANCE = new AppModule_ProvideNotificationHelperFactory();
  }
}
