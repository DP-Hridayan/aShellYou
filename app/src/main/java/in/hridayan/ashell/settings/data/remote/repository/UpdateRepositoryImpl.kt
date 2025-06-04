package `in`.hridayan.ashell.settings.data.remote.repository

import android.util.Log
import `in`.hridayan.ashell.settings.data.remote.api.GitHubApi
import `in`.hridayan.ashell.settings.data.remote.mapper.toDomain
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import `in`.hridayan.ashell.settings.domain.repository.UpdateRepository
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.utils.io.errors.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    private val api: GitHubApi
) : UpdateRepository {

    override suspend fun fetchLatestRelease(includePrerelease:Boolean): UpdateResult {
        return try {
            Log.d("UpdateRepository", "Fetching latest release from GitHub")
            val response = api.fetchLatestRelease(includePrerelease).toDomain()
            UpdateResult.Success(response, response.tagName.isNotEmpty())
        } catch (e: SocketTimeoutException) {
            Log.e("UpdateRepository", "Timeout", e)
            UpdateResult.Timeout
        } catch (e: UnknownHostException) {
            Log.e("UpdateRepository", "No internet (UnknownHost)", e)
            UpdateResult.NetworkError
        } catch (e: ConnectException) {
            Log.e("UpdateRepository", "No internet (ConnectException)", e)
            UpdateResult.NetworkError
        } catch (e: UnresolvedAddressException) {
            Log.e("UpdateRepository", "No internet (UnresolvedAddressException)", e)
            UpdateResult.NetworkError
        } catch (e: IOException) {
            Log.e("UpdateRepository", "IO Exception", e)
            UpdateResult.NetworkError
        } catch (e: Exception) {
            Log.e("UpdateRepository", "Unknown error", e)
            UpdateResult.UnknownError
        }
    }
}