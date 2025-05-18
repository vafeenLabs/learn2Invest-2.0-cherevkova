package ru.surf.learn2invest.presentation.ui.components.screens.fragments.asset_review

import android.widget.ImageView

internal sealed class AssetReviewActivityIntent {
    data object OpenAssetOverViewFragment : AssetReviewActivityIntent()
    data object OpenSubHistoryFragment : AssetReviewActivityIntent()
    data object GoBack : AssetReviewActivityIntent()
    data class LoadIcon(val imageView: ImageView) : AssetReviewActivityIntent()
    data object CancelLoadingImage : AssetReviewActivityIntent()
}