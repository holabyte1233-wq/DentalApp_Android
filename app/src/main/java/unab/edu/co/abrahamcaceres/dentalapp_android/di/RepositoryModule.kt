package unab.edu.co.abrahamcaceres.dentalapp_android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import unab.edu.co.abrahamcaceres.dentalapp_android.data.repository.AuthRepositoryImpl
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.AuthRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
