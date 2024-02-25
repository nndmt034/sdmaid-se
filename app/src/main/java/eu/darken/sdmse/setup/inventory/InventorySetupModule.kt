package eu.darken.sdmse.setup.inventory

import android.content.Context
import android.content.Intent
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import eu.darken.sdmse.common.coroutine.AppScope
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.flow.replayingShare
import eu.darken.sdmse.common.hasApiLevel
import eu.darken.sdmse.common.permissions.Permission
import eu.darken.sdmse.common.pkgs.getSettingsIntent
import eu.darken.sdmse.common.pkgs.toPkgId
import eu.darken.sdmse.common.rngString
import eu.darken.sdmse.setup.SetupModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventorySetupModule @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    @ApplicationContext private val context: Context,
) : SetupModule {

    private val refreshTrigger = MutableStateFlow(rngString)
    override val state = refreshTrigger
        .mapLatest {
            val requiredPermission = getRequiredPermission()

            val missingPermission = requiredPermission.filter {
                val isGranted = it.isGranted(context)
                log(TAG) { "${it.permissionId} isGranted=$isGranted" }
                !isGranted
            }.toSet()

            return@mapLatest State(
                missingPermission = missingPermission,
                settingsIntent = context.packageName.toPkgId().getSettingsIntent(context)
            )
        }
        .replayingShare(appScope)

    private fun getRequiredPermission(): Set<Permission> = when {
        hasApiLevel(34) -> setOf(Permission.QUERY_ALL_PACKAGES)
        else -> emptySet()
    }

    override suspend fun refresh() {
        log(TAG) { "refresh()" }
        refreshTrigger.value = rngString
    }

    data class State(
        val missingPermission: Set<Permission>,
        val settingsIntent: Intent,
    ) : SetupModule.State {

        override val type: SetupModule.Type
            get() = SetupModule.Type.INVENTORY

        override val isComplete: Boolean = missingPermission.isEmpty()

    }

    @Module @InstallIn(SingletonComponent::class)
    abstract class DIM {
        @Binds @IntoSet abstract fun mod(mod: InventorySetupModule): SetupModule
    }

    companion object {
        private val TAG = logTag("Setup", "Inventory", "Module")
    }
}