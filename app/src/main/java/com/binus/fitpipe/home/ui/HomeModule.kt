package com.binus.fitpipe.home.ui

import com.binus.fitpipe.home.data.HomeRepository
import com.binus.fitpipe.home.data.HomeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class HomeModule {
    @Binds
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}
