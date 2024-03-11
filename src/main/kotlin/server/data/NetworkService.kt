package server.data

import interfaces.data.INetworkService
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class NetworkService : INetworkService {

    override fun sendHttpRequest(url: String): String? {
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Error) {
            println("[-] error send HttpRequest: ${e.message}")
            return null
        }
        return response.body()
    }

    override fun sendPostHttpRequest(url: String, body: String): String? {
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Error) {
            println("[-] error send PostRequest: ${e.message}")
            return null
        }
        return response.body()
    }

    override fun sendGetHttpRequest(url: String): InputStream? {
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response: HttpResponse<InputStream> = try {
            HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofInputStream())
        } catch (e: Error) {
            println("[-] error send Get Request: ${e.message}")
            return null
        }
        println("[${response.statusCode()}] code response:")
        return response.body()
    }

}