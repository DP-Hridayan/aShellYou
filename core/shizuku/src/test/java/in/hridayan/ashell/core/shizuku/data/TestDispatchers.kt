package `in`.hridayan.ashell.core.shizuku.data

import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

class TestDispatchers(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val io = dispatcher
    override val default = dispatcher
    override val unconfined = dispatcher
}
