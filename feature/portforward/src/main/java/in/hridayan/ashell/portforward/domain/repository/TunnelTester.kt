package `in`.hridayan.ashell.portforward.domain.repository

import `in`.hridayan.ashell.portforward.domain.model.TestResult
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget

/** Checks whether a tunnel's target accepts connections, before the tunnel is started. */
interface TunnelTester {
    suspend fun test(target: TunnelTarget): TestResult
}
