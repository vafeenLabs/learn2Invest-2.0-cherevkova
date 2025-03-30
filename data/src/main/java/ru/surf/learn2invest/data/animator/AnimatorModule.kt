package ru.surf.learn2invest.data.animator

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import ru.surf.learn2invest.domain.animator.CustomAnimator

@Module
@InstallIn(ViewModelComponent::class)
internal class AnimatorModule {

    @Provides
    fun provideAnimator(impl: CustomAnimatorImpl): CustomAnimator = impl
}