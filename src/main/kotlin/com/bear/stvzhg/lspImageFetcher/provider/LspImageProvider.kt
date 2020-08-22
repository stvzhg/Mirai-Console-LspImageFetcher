package com.bear.stvzhg.lspImageFetcher.provider

data class LspImageInput(
    val isR18: Boolean = false,
    val pageSize: Int = 1,
    val isFast: Boolean = false)

data class LspImage (
    var pid: Long = 0L,
    var uid: Long = 0L,
    var auther: String = "",
    var title: String = "",
    var isR18: Boolean = false,
    var picUrl: String = "",
    var webUrl: String = "",
    var tags: ArrayList<String> = arrayListOf("")
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
        result = (31 * result + uid) % Int.MAX_VALUE
        return result.toInt()
    }
}

interface LspImageProvider {
    fun getImage(input: LspImageInput): LspImage {
        return LspImage()
    }
}