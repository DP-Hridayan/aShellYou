package `in`.hridayan.ashell.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuHelperPolicy
import `in`.hridayan.ashell.shizuku.AppShizukuHelperPolicy
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShizukuHelperPolicyModule {

    @Binds
    @Singleton
    abstract fun bindShizukuHelperPolicy(impl: AppShizukuHelperPolicy): ShizukuHelperPolicy
}
