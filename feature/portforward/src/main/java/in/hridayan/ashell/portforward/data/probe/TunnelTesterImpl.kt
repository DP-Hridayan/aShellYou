package `in`.hridayan.ashell.portforward.data.probe

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.portforward.data.engine.DeviceTargetConnector
import `in`.hridayan.ashell.portforward.data.engine.closeQuietly
import `in`.hridayan.ashell.portforward.di.PortForwardDispatcher
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.TestResult
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.toTunnelError
import `in`.hridayan.ashell.portforward.domain.repository.LocalNetworkGate
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettingsSource
import `in`.hridayan.ashell.portforward.domain.repository.TunnelTester
import `in`.hridayan.ashell.portforward.domain.usecase.MapTunnelErrorUseCase
import `in`.hridayan.ashell.portforward.domain.usecase.ParseHttpHeadUseCase
import `in`.hridayan.ashell.portforward.domain.usecase.ParseHttpHeadUseCase.HttpHead
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

/**
 * Connects to a target, then sends an HTTP `HEAD` request to learn whether it is a web server.
 *
 * A target that accepts the connection but does not answer HTTP is still reachable; it just cannot
 * be opened in a browser.
 */
class TunnelTesterImpl @Inject constructor(
    private val localNetwork: LocalNetworkGate,
    private val deviceConnector: DeviceTargetConnector,
    private val settings: PortForwardSettingsSource,
    private val mapError: MapTunnelErrorUseCase,
    private val parseHttpHead: ParseHttpHeadUseCase,
    @param:PortForwardDispatcher private val dispatcher: CoroutineDispatcher
) : TunnelTester {

    override suspend fun test(target: TunnelTarget): TestResult = when (target) {
        is TunnelTarget.NetworkHost -> testHost(target)
        is TunnelTarget.Device -> testDevice(target)
    }

    private suspend fun testDevice(target: TunnelTarget.Device): TestResult {
        val startedAt = System.nanoTime()
        val stream = deviceConnector.open(target).getOrElse { return TestResult.Unreachable(it.toTunnelError()) }
        val latencyMillis = (System.nanoTime() - startedAt) / NANOS_PER_MILLI
        val head = try {
            if (target.endpoint.type in HTTP_CAPABLE_ENDPOINTS) requestHead(stream) else null
        } finally {
            stream.close()
        }
        return TestResult.Reachable(latencyMillis, head?.status, head?.server)
    }

    private suspend fun requestHead(stream: AdbDuplexStream): HttpHead? = try {
        withTimeoutOrNull(HTTP_READ_TIMEOUT_MS.toLong()) {
            stream.write(headRequest(LOCALHOST, ROOT_PATH).toByteArray(Charsets.ISO_8859_1))
            parseHttpHead(readHead(stream))
        }
    } catch (_: IOException) {
        null
    }

    private suspend fun readHead(stream: AdbDuplexStream): List<String> {
        val collected = StringBuilder()
        while (!collected.contains(HEAD_END) && collected.length < MAX_HEAD_CHARS) {
            collected.append(String(stream.read(), Charsets.ISO_8859_1))
        }
        return collected.toString().substringBefore(HEAD_END).lines()
    }

    private suspend fun testHost(target: TunnelTarget.NetworkHost): TestResult {
        if (localNetwork.isBlocked(target.host)) return TestResult.Unreachable(TunnelError.LocalNetworkBlocked)
        val timeoutMillis = settings.current().connectTimeoutMillis
        return runInterruptible(dispatcher) { probe(target, timeoutMillis) }
    }

    private fun probe(target: TunnelTarget.NetworkHost, timeoutMillis: Int): TestResult {
        val socket = Socket()
        return try {
            val startedAt = System.nanoTime()
            socket.connect(InetSocketAddress(target.host, target.port), timeoutMillis)
            val latencyMillis = (System.nanoTime() - startedAt) / NANOS_PER_MILLI
            val head = requestHead(socket, target)
            TestResult.Reachable(latencyMillis, head?.status, head?.server)
        } catch (e: IOException) {
            TestResult.Unreachable(mapError.forConnect(e))
        } finally {
            socket.closeQuietly()
        }
    }

    private fun requestHead(socket: Socket, target: TunnelTarget.NetworkHost): HttpHead? = try {
        socket.soTimeout = HTTP_READ_TIMEOUT_MS
        socket.getOutputStream().apply {
            write(headRequest(target).toByteArray(Charsets.ISO_8859_1))
            flush()
        }
        parseHttpHead(readHead(socket))
    } catch (_: IOException) {
        null
    }

    private fun readHead(socket: Socket): List<String> {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.ISO_8859_1))
        return generateSequence { reader.readLine() }
            .takeWhile { it.isNotEmpty() }
            .take(MAX_HEADER_LINES)
            .toList()
    }

    private fun headRequest(target: TunnelTarget.NetworkHost): String {
        val host = if (target.host.contains(':')) "[${target.host}]" else target.host
        return headRequest("$host:${target.port}", target.path ?: ROOT_PATH)
    }

    private fun headRequest(hostHeader: String, path: String): String =
        "HEAD $path HTTP/1.1\r\nHost: $hostHeader\r\nConnection: close\r\n\r\n"

    private companion object {
        const val HTTP_READ_TIMEOUT_MS = 3_000
        const val NANOS_PER_MILLI = 1_000_000L
        const val MAX_HEADER_LINES = 100
        const val ROOT_PATH = "/"
        const val LOCALHOST = "localhost"
        const val HEAD_END = "\r\n\r\n"
        const val MAX_HEAD_CHARS = 16 * 1024
        val HTTP_CAPABLE_ENDPOINTS = setOf(DeviceEndpointType.TCP, DeviceEndpointType.ABSTRACT)
    }
}
