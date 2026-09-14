package `in`.hridayan.ashell.qstiles.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.qstiles.data.repository.MaterialIconRepositoryImpl
import `in`.hridayan.ashell.qstiles.domain.repository.MaterialIconRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MaterialIconModule {

    @Binds
    @Singleton
    abstract fun bindMaterialIconRepository(
        impl: MaterialIconRepositoryImpl,
    ): MaterialIconRepository
}
