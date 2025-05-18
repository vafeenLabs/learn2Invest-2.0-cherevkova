package ru.surf.learn2invest.presentation.ui.components.screens.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ru.surf.learn2invest.domain.cryptography.FingerprintAuthenticator
import ru.surf.learn2invest.domain.utils.launchMAIN
import ru.surf.learn2invest.presentation.R
import ru.surf.learn2invest.presentation.databinding.FragmentProfileBinding
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.delete_profile.DeleteProfileDialog
import ru.surf.learn2invest.presentation.ui.components.alert_dialogs.reset_stats.ResetStatsDialog
import ru.surf.learn2invest.presentation.ui.components.screens.sign_in.common.AuthActivity
import ru.surf.learn2invest.presentation.ui.components.screens.trading_password.common.TradingPasswordActivity
import ru.surf.learn2invest.presentation.utils.setStatusBarColor
import javax.inject.Inject

/**
 * Фрагмент профиля в [HostActivity][ru.surf.learn2invest.presentation.ui.components.screens.host.HostActivity]
 */
@AndroidEntryPoint
internal class ProfileFragment : Fragment() {
    private val viewModel: ProfileFragmentViewModel by viewModels()

    @Inject
    lateinit var fingerprintAuthenticator: FingerprintAuthenticator
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        activity?.apply {
            setStatusBarColor(window, this, R.color.white, R.color.main_background_dark)
        }

        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        initListeners(binding)
        return binding.root
    }

    private fun initListeners(binding: FragmentProfileBinding) {
        lifecycleScope.launchMAIN {
            viewModel.state.collect {
                binding.apply {
                    firstNameLastNameTV.text = it.let { pr -> "${pr.firstName}\n${pr.lastName}" }
                    confirmDealBtnSwitcher.isChecked = it.tradingPasswordHash != null
                    changeTradingPasswordBtn.isVisible = it.tradingPasswordHash != null
                    biometryBtn.isVisible = it.isBiometryAvailable
                    biometryBtnSwitcher.isChecked = it.biometry
                }
            }
        }
        viewModel.handleIntent(ProfileFragmentIntent.IsBiometricAvailable)
        lifecycleScope.launchMAIN {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ProfileFragmentEffect.ShowDeleteProfileDialogEffect -> DeleteProfileDialog()
                        .showDialog(parentFragmentManager)

                    ProfileFragmentEffect.ShowResetStatsDialogEffect -> ResetStatsDialog()
                        .showDialog(parentFragmentManager)

                    ProfileFragmentEffect.TradingPasswordActivityChangeTP -> {
                        requireActivity().startActivity(
                            TradingPasswordActivity.newInstanceChangeTP(
                                requireActivity() as AppCompatActivity
                            )
                        )
                    }

                    ProfileFragmentEffect.TradingPasswordActivityCreateTP ->
                        requireActivity().startActivity(
                            TradingPasswordActivity.newInstanceCreateTP(
                                requireActivity() as AppCompatActivity
                            )
                        )

                    ProfileFragmentEffect.TradingPasswordActivityRemoveTP ->
                        requireActivity().startActivity(
                            TradingPasswordActivity.newInstanceRemoveTP(
                                requireActivity() as AppCompatActivity
                            )
                        )

                    ProfileFragmentEffect.SignInActivityChangingPIN ->
                        requireActivity().startActivity(
                            AuthActivity.newInstanceChangingPIN(
                                requireActivity() as AppCompatActivity
                            )
                        )

                    is ProfileFragmentEffect.FingerPrintBottomSheet -> {
                        fingerprintAuthenticator.setDesignBottomSheet(
                            title = getString(R.string.biometry),
                            cancelText = getString(R.string.cancel),
                        ).setSuccessCallback {
                            effect.onSuccess()
                        }.setCancelCallback {
                            effect.onCancel()
                        }.setFailedCallback {
                            effect.onError()
                        }.auth(requireActivity() as AppCompatActivity)
                    }
                }
            }
        }
        binding.also { fr ->
            fr.deleteProfileTV.setOnClickListener {
                viewModel.handleIntent(ProfileFragmentIntent.ShowDeleteProfileDialogEffect)
            }

            fr.resetStatsBtn.setOnClickListener {
                viewModel.handleIntent(ProfileFragmentIntent.ShowResetStatsDialogEffect)
            }

            fr.biometryBtn.setOnClickListener {
                viewModel.handleIntent(ProfileFragmentIntent.BiometryBtnSwitch)
            }

            fr.changeTradingPasswordBtn.setOnClickListener {
                viewModel.handleIntent(ProfileFragmentIntent.ChangeTradingPassword)
            }

            fr.confirmDealBtn.setOnClickListener {
                viewModel.handleIntent(ProfileFragmentIntent.ChangeTransactionConfirmation)
            }

            fr.changePINBtn.setOnClickListener {
                viewModel.handleIntent(ProfileFragmentIntent.ChangePIN)
            }
        }
    }
}