package ru.surf.learn2invest.data.cryptography

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import ru.surf.learn2invest.domain.cryptography.FingerprintAuthenticator
import ru.surf.learn2invest.domain.cryptography.PasswordHasher

@Module
@InstallIn(ViewModelComponent::class, FragmentComponent::class, ActivityRetainedComponent::class)
internal class CryptographyDIModule {

    @Provides
    fun bindFingerprintAuthenticator(
        impl: FingerprintAuthenticatorImpl
    ): FingerprintAuthenticator = impl

    @Provides
    fun providePasswordHasher(impl: PasswordHasherImpl): PasswordHasher = impl
}
