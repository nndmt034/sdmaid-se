package eu.darken.sdmse.main.ui.onboarding.privacy

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.sdmse.common.PrivacyPolicy
import eu.darken.sdmse.common.WebpageTool
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.datastore.valueBlocking
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.uix.ViewModel3
import eu.darken.sdmse.main.core.GeneralSettings
import eu.darken.sdmse.main.ui.dashboard.items.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class OnboardingPrivacyFragmentVM @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val generalSettings: GeneralSettings,
    private val webpageTool: WebpageTool,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    val isBugReporterEnabled = generalSettings.isBugReporterEnabled.flow.asLiveData2()

    fun goPrivacyPolicy() {
        log(TAG) { "goPrivacyPolicy()" }
        webpageTool.open(PrivacyPolicy.URL)
    }

    fun setBugReportingEnabled(enabled: Boolean) {
        log(TAG) { "setBugReportingEnabled($enabled)" }
        generalSettings.isBugReporterEnabled.valueBlocking = enabled
    }

    companion object {
        private val TAG = logTag("Onboarding", "Privacy", "Fragment", "VM")
    }
}