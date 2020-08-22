package com.bear.stvzhg.lspImageFetcher.provider

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

import net.mamoe.mirai.utils.MiraiLogger

class LCImageProvider(
    val apiKey: String,
    val logger: MiraiLogger
): LspImageProvider {
    val webUrlPrefix = "https://www.pixiv.net/artworks/"
    override fun getImage(input: LspImageInput): LspImage {
        var image = LspImage()
        Fuel
            .get(
            "https://api.lolicon.app/setu/",
            listOf(
                "apikey" to apiKey,
                "r18" to if (input.isR18) "1" else "0",
                "num" to input.pageSize.toString(),
                "size1200" to input.isFast.toString()
            )
        )
            .responseObject(LoliconOutput.Deserializer()) {
                request, response, result ->
                val (output, err) = result
                if (output != null) {
                    if (output.count == 0 || output.data.size == 0) {
                        throw IllegalStateException("No Image is returned from API")
                    } else {
                        val imageData = output.data[0]
                        image = LspImage(
                            imageData.pid,
                            imageData.uid,
                            imageData.author,
                            imageData.title,
                            imageData.r18,
                            imageData.url,
                            webUrlPrefix.plus(imageData.pid.toString()),
                            imageData.tags
                        )
                    }
                }
            }.join()
        return image
    }
}

data class LoliconOutput(
    val code: Int = 0,
    val msg: String = "",
    val quota: Int = 0,
    val quota_min_ttl: Long = 0L,
    val count: Int = 1,
    val data: ArrayList<ImageData> = arrayListOf()) {

    class Deserializer : ResponseDeserializable<LoliconOutput> {
        override fun deserialize(content: String) = Gson().fromJson(content, LoliconOutput::class.java)
    }
}

data class ImageData(
    val pid: Long = 0L,
    val p: Long = 0L,
    val uid: Long = 0L,
    val title: String = "",
    val author: String = "",
    val url: String = "",
    val r18: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    val tags: ArrayList<String> = arrayListOf()
)