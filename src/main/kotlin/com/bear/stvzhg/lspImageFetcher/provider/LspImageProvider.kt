package com.bear.stvzhg.lspImageFetcher.provider

data class LspImageInput(
    val isR18: Boolean = false,
    val pageSize: Int = 1,
    val isFast: Boolean = false,
    val keyword: String = "")

data class LspImage (
    val pid: Long = 0L,
    val uid: Long = 0L,
    val auther: String = "",
    val title: String = "",
    val isR18: Boolean = false,
    val picUrl: String = "",
    val webUrl: String = "",
    val tags: ArrayList<String> = arrayListOf("")
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as com.bear.stvzhg.lspImageFetcher.provider.LspImage

        if (pid != other.pid) return false
        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pid
        result = (result xor uid) % Int.MAX_VALUE
        return result.toInt()
    }
}

interface LspImageProvider {
    fun getImage(input: LspImageInput): LspImage {
        return LspImage()
    }
}