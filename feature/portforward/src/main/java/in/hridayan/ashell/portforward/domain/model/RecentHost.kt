package `in`.hridayan.ashell.portforward.domain.model

/** A host the user relayed to before, offered as a shortcut in the tunnel editor. */
data class RecentHost(val host: String, val lastPort: Int)
