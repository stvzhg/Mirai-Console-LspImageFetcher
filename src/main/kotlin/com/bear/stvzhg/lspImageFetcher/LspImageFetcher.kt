package com.bear.stvzhg.lspImageFetcher

import com.bear.stvzhg.lspImageFetcher.provider.LspImage
import com.bear.stvzhg.lspImageFetcher.provider.LspImageInput
import com.bear.stvzhg.lspImageFetcher.provider.LCImageProvider
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResponseResult
import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.command.ContactCommandSender
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

object LspImageFetcher : PluginBase() {

    val config = loadConfig("setting.yml")

    val normalImageTrigger by config.withDefaultWriteSave { "һ��ɫͼ" }
    val r18ImageTrigger by config.withDefaultWriteSave { "����ɫ" }
    val useSmallImage by config.withDefaultWriteSave { true }
    val apiKey by config.withDefaultWriteSave { "" }
    val privateUse by config.withDefaultWriteSave { false }

    val allowGroupNormal = config.getLongList("allowGroupNormal").toMutableList()
    val allowGroupR18 = config.getLongList("allowGroupR18").toMutableList()
    val imageProvider = LCImageProvider(apiKey, logger)

    override fun onLoad() {
        super.onLoad()
    }

    override fun onDisable() {
        super.onDisable()
        logger.info("LspImageFetcher: Saving config data")
        config["allowGroupNormal"] = allowGroupNormal
        config["allowGroupR18"] = allowGroupR18
        config.save()
    }

    override fun onEnable() {
        super.onEnable()
        logger.info("Lsp Plugin loaded!")
        registerCommands()
        logger.info("Lsp Plugin commands registered!")

        if (privateUse) {
            subscribeFriendMessages {
                (contains(r18ImageTrigger)) {
                    val image = getImage(true, false, this.sender.id)
                    reply(getImagePromote(image, sender.nick))
                    sendImage(downloadImage(image.picUrl))
                }
                (contains(normalImageTrigger)) {
                    val image = getImage(false, false, this.sender.id)
                    reply(getImagePromote(image, sender.nick))
                    sendImage(downloadImage(image.picUrl))
                }
            }
        }

        subscribeGroupMessages {
            (contains(r18ImageTrigger)) {
                if (allowGroupR18.contains(this.group.id)) {
                    val image = getImage(true, true, this.sender.id)
                    reply(getImagePromote(image, sender.nick))
                    sendImage(downloadImage(image.picUrl))
                }
            }
            (contains(normalImageTrigger)) {
                if (allowGroupNormal.contains(this.group.id)) {
                    val image = getImage(false, true, this.sender.id)
                    reply(getImagePromote(image, sender.nick))
                    sendImage(downloadImage(image.picUrl))
                }
            }
        }
    }

    private suspend fun getImage(isR18: Boolean, isGroup: Boolean, requesterId: Long): LspImage {
        logger.info("Image request from qq id: $requesterId")
        val input = LspImageInput(
            isR18,
            1,
            useSmallImage
        )
        return imageProvider.getImage(input)
    }

    private fun getImagePromote(image: LspImage, senderName:String): String {
        return "һ��ɫͼ�͸� ${senderName}\n" +
                "����: ${image.title}\n" +
                "����: ${image.auther}\n" +
                "pid: ${image.pid}\n" +
                "uid: ${image.uid}\n" +
                "��Ʒ����: ${image.webUrl}\n" +
                "Tag: ${image.tags.joinToString(",")}"
    }

    private suspend fun downloadImage(webUrl: String): InputStream {
        val outputStream = ByteArrayOutputStream()
        Fuel.download(webUrl)
            .streamDestination { response, request ->
                Pair(outputStream,{ByteArray(0).inputStream()})
            }
            .appendHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36")
            .appendHeader("referer", "https://www.pixiv.net/")
            .awaitByteArrayResponseResult()
        return ByteArrayInputStream(outputStream.toByteArray())
    }

    private fun registerCommands() {
        registerCommand {
            name = "lsp"
            alias = listOf("setu")
            description = "lsp plugin management"
            usage = "/lsp enable [qq group id] �����ڱ�Ⱥ������ͨͼƬ\n" +
                    "/lsp enableR18 [qq group id] �����ڱ�Ⱥ����r18ͼƬ\n" +
                    "��һ����������config�����apikey"
            onCommand {
                if (this is ContactCommandSender) {return@onCommand false}
                if (it.isEmpty()) {
                    return@onCommand false
                }
                when (it[0]) {
                    "enable" -> {
                        if (it.size < 2) {
                            return@onCommand false
                        } else {
                            val to = try {
                                it[1].toLong()
                            } catch (e: Throwable) {
                                this.sendMessage("${it[1]}�޷�ת��Ϊ����")
                                return@onCommand false
                            }
                            allowGroupNormal.add(to)
                            config.save()
                            this.sendMessage("������ ${to.toString()} ������ͨͼƬ")
                            return@onCommand true
                        }
                    }
                    "enableR18" -> {
                        if (it.size < 2) {
                            return@onCommand false
                        } else {
                            val to = try {
                                it[1].toLong()
                            } catch (e: Throwable) {
                                this.sendMessage("${it[1]}�޷�ת��Ϊ����")
                                return@onCommand false
                            }
                            allowGroupR18.add(to)
                            config.save()
                            this.sendMessage("������ ${to.toString()} ����R18ͼƬ")
                            return@onCommand true
                        }
                    }
                    else -> {
                        return@onCommand false
                    }
                }
            }
        }
    }
}