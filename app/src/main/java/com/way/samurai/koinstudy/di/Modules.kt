package com.way.samurai.koinstudy.di

import com.squareup.moshi.Moshi
import com.way.samurai.koinstudy.data.dispatchers.AppDispatchers
import com.way.samurai.koinstudy.data.dispatchers.DispatcherProvider
import com.way.samurai.koinstudy.data.remote.FakeApi
import com.way.samurai.koinstudy.data.remote.TaskRemoteDataSource
import com.way.samurai.koinstudy.data.repository.UserRepositoryImpl
import com.way.samurai.koinstudy.domain.repository.UserRepository
import com.way.samurai.koinstudy.domain.usecase.GetUserUseCase
import com.way.samurai.koinstudy.domain.usecase.RefreshUserUseCase
import com.way.samurai.koinstudy.presentation.scoped.TaskWizardTracker
import com.way.samurai.koinstudy.presentation.state.UserViewModel
import com.way.samurai.koinstudy.presentation.state.details.UserDetailsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val coreModule = module {
    singleOf(::AppDispatchers) { bind<DispatcherProvider>() }
}

val networkModule = module {
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }
    single { Moshi.Builder().build() }
    single {
        Retrofit.Builder()
            .baseUrl("https://example.com/") // not used, FakeApi still mirrors Retrofit shape
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }
    singleOf(::FakeApi)
}

val dataModule = module {
    singleOf(::TaskRemoteDataSource)
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
}

val featureTasksModule = module {
    factory { GetUserUseCase(get()) }
    factory { RefreshUserUseCase(get()) }

    // Example of feature scope for a wizard/flow
    scope(named(TaskWizardTracker.SCOPE_NAME)) {
        scoped { TaskWizardTracker() }
    }

    viewModel {
        UserViewModel(
            getUser = get(),
            refreshUser = get(),
            dispatchers = get(),
        )
    }

    viewModel { (userId: String) ->
        UserDetailsViewModel(
            userId = userId,
            getUser = get(),
        )
    }
}
