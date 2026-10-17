package `in`.hridayan.ashell.portforward.di

import javax.inject.Qualifier

/**
 * The dispatcher that tunnel sockets block on.
 *
 * Every relayed connection keeps two threads parked in blocking reads, so tunnels get their own
 * pool instead of the shared IO one, where they could starve mirroring and the shell.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PortForwardDispatcher
