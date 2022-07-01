package kim.uno.mock.extension

import okhttp3.Response
import java.nio.charset.StandardCharsets

fun Response.peekBody(): String? {
    return body?.let { body ->
        val source = body.source().apply { request(Long.MAX_VALUE) }
        val buffer = source.buffer
        val contentType = body.contentType()
        val charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
        return buffer.clone().readString(charset)
    }
}
