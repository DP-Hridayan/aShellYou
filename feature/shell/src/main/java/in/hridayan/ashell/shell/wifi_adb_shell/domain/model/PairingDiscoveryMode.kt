package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * Which pairing scan, if any, should be running.
 *
 * Only one scan runs at a time. Holding this as a single value lets the pairing screen own the scan
 * lifecycle from one place, instead of each tab starting and stopping its own scan as the pager
 * composes and discards page content.
 */
enum class PairingDiscoveryMode {
    None,
    Qr,
    Code
}
