package kim.uno.mock.util

import android.util.Log
import androidx.collection.ArrayMap
import kim.uno.mock.BuildConfig
import kim.uno.mock.extension.enableTlsSocketFactory
import kim.uno.mock.extension.peekBody
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class Retrofit {

    abstract val domain: String
    abstract val enableTls: Boolean

    open val headers: ArrayMap<String, String>? = null

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {

            if (enableTls) {
                enableTlsSocketFactory()
            }

            // header append
            addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                headers?.forEach { header ->
                    builder.addHeader(header.key, header.value)
                }

                proceed(chain, builder.build())
            }

            if (BuildConfig.DEBUG) {
                // okhttp3
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }

        }.build()
    }

    open fun proceed(chain: Interceptor.Chain, request: Request): Response {
        try {
            return chain.proceed(request).also { response ->
                // logger
                if (BuildConfig.DEBUG) {
                    val logLevel = if (response.isSuccessful) Log.VERBOSE else Log.ERROR
                    Logger(level = logLevel).apply {
                        header("${request.method} ${response.code} ${request.url}")
                        line(request.url.toString().split(domain)[1].split("?")[0])
                        request.headers.forEach { header ->
                            line("${header.first}: ${header.second}")
                        }
                        response.peekBody()
                            .takeIf { !it.isNullOrBlank() }
                            ?.let {
                                section()
                                line(it)
                            }
                    }.show()
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Logger(level = Log.ERROR).apply {
                    header("${request.method} ${request.url}")
                    line(request.url.toString().split(domain)[1].split("?")[0])
                    request.headers.forEach { header ->
                        line("${header.first}: ${header.second}")
                    }
                    section()
                    throwable(e)
                }.show()
            }
            throw e
        }
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(domain)
            .addConverterFactory(GsonConverterFactory.create(GsonUtil.gson))
            .client(okHttpClient)
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    fun <T> errorBodyAs(errorBodyClass: Class<T>, errorBody: ResponseBody?): T? {
        return try {
            retrofit.responseBodyConverter<T>(
                errorBodyClass,
                errorBodyClass.annotations
            ).convert(errorBody)
        } catch (e: Exception) {
            null
        }
    }

}
