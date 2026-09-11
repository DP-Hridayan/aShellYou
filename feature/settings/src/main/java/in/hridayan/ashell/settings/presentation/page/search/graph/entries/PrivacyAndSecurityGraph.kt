package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.privacyAndSecurityGraph(
    navController: NavController,
    requireAuth: Boolean,
    timeoutText: String = ""
) {
    screen(
        id = SettingsGraphId.PRIVACY_SECURITY,
        title = R.string.privacy_and_security,
        navigate = {
            navController.navigate(NavRoutes.PrivacySecurityScreen) {
                launchSingleTop = true
            }
        }
    ) {
        entry(key = SettingsKeys.RequireAuthentication) {
            title(R.string.require_authentication)
            description(R.string.des_require_authentication)
            icon(if (requireAuth) R.drawable.ic_encrypted else R.drawable.ic_encrypted_off)
        }

        entry(key = SettingsKeys.UseBiometrics) {
            title(R.string.use_biometrics)
            description(R.string.des_use_biometrics)
            icon(if (requireAuth) R.drawable.ic_fingerprint else R.drawable.ic_fingerprint_off)
            availableWhen(requireAuth)
        }

        entry(key = SettingsKeys.AuthenticationTimeout) {
            title(R.string.authentication_timeout)
            description(timeoutText)
            icon(R.drawable.ic_lock_clock)
            availableWhen(requireAuth)
        }

        entry(key = SettingsKeys.RequireAuthenticationForBackups) {
            title(R.string.require_auth_for_backups)
            description(R.string.des_require_auth_for_backups)
            icon(R.drawable.ic_sync_lock)
            availableWhen(requireAuth)
        }

        entry(key = SettingsKeys.AppPermissions) {
            title(R.string.app_permissions)
            description(R.string.des_app_permissions)
            icon(R.drawable.ic_search_gear)
        }

        entry(key = SettingsKeys.PrivacyPolicy) {
            title(R.string.privacy_policy)
            description(R.string.des_privacy_policy)
            icon(R.drawable.ic_policy)
        }

        entry(key = SettingsKeys.Licenses) {
            title(R.string.libraries_and_licenses)
            description(R.string.des_licenses)
            icon(R.drawable.ic_license)
        }
    }
}