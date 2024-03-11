package interfaces.data

import java.io.InputStream

interface INetworkService {

    fun sendHttpRequest(url: String): String?
    fun sendPostHttpRequest(url: String, body: String): String?
    fun sendGetHttpRequest(url: String): InputStream?

}