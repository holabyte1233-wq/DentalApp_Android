package unab.edu.co.abrahamcaceres.dentalapp_android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import unab.edu.co.abrahamcaceres.dentalapp_android.data.repository.AuthRepositoryImpl
import unab.edu.co.abrahamcaceres.dentalapp_android.data.repository.PatientRepositoryImpl
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.AuthRepository
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.PatientRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindPatientRepository(impl: PatientRepositoryImpl): PatientRepository
}
