package me.sailex.mineskinproxy

import com.fasterxml.jackson.core.JacksonException
import com.google.gson.Gson
import io.javalin.Javalin
import org.mineskin.Java11RequestHandler
import org.mineskin.MineSkinClient
import org.mineskin.data.CodeAndMessage
import org.mineskin.data.JobInfo
import org.mineskin.data.Variant
import org.mineskin.data.Visibility
import org.mineskin.exception.MineSkinRequestException
import org.mineskin.request.GenerateRequest
import java.util.Optional

class MineSkinAPI(
    private val apiKey: String,
    private val userAgent: String
) {
    private val gson = Gson()
    private val app: Javalin = Javalin.create()
    private var skinsResolvedCount = 0

    init {
        val client: MineSkinClient = MineSkinClient.builder()
            .apiKey(apiKey)
            .requestHandler(::Java11RequestHandler)
            .userAgent(userAgent)
            .build()
        defineEndpoint(client)
    }

    fun startListening(port: Int) {
        app.start(port)
    }

    private fun defineEndpoint(client: MineSkinClient) {
        app.post("/skin") { ctx ->
            var request: SkinRequest? = null
            try {
                request = ctx.bodyAsClass(SkinRequest::class.java)
            } catch (e: JacksonException) {
                println(e)
                ctx.status(400).result("Invalid request body: ${e.message}")
            }

            val requestBody: GenerateRequest = GenerateRequest.url(request?.url)
                .visibility(Visibility.UNLISTED)

            val result = client.queue().submit(requestBody).thenCompose { queueResponse ->
                val job: JobInfo = queueResponse.job
                job.waitForCompletion(client)
            }.thenCompose { jobResponse -> jobResponse.getOrLoadSkin(client) }
            .thenApply { skinInfo ->
                SkinResult(
                    skinInfo.texture().data().value(),
                    skinInfo.texture().data().signature(),
                    request?.variant ?: Variant.AUTO,
                )
            }
            .exceptionally { throwable ->
                if (throwable is MineSkinRequestException) {
                    val detailsOptional: Optional<CodeAndMessage> = throwable.response.errorOrMessage
                    detailsOptional.ifPresent { details ->
                        ctx.status(500).result(details.code() + ": " + details.message())
                    }
                }
                null
            }.join()

            if (result != null) {
                ctx.status(200).result(gson.toJson(result))
                skinsResolvedCount++
            } else {
                ctx.status(400).result("No result")
            }
        }

        app.get("/") {
            ctx -> ctx.result("hey! resolvedSkins: $skinsResolvedCount")
        }
    }
}
