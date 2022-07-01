# Retrofit
- Retrofit abstract class 
- 보일러플레이트 제거
- TLS 추가
- Logger 전문 출력
- [GithubRetrofit.kt](https://github.com/ikmuwn/Mock-android/blob/develop/app/src/main/java/kim/uno/mock/data/remote/GithubRetrofit.kt)
- [GithubData.kt#L10](https://github.com/ikmuwn/Mock-android/blob/96e5d1154c2a9fb5c8c2ba77cda232ee27f17345/app/src/main/java/kim/uno/mock/data/remote/github/GithubData.kt#L10)
- [Mock-andriod](https://github.com/ikmuwn/Mock-android)

## Use

- Retrofit 구조체 정의

  ```kotlin
  class GithubRetrofit @Inject constructor() : Retrofit() {

      // 호스트 서버 지정
      override val domain = DOMAIN

      // tls 활성
      override val enableTls = true

      // 공통 헤더값 지정
      override val headers: ArrayMap<String, String>
          get() = Companion.headers

      override fun proceed(chain: Interceptor.Chain, request: Request): Response {
          val response = super.proceed(chain, request)
          if (!response.isSuccessful) {
              // 자체 에러 전문으로 커스텀 오류 throw 가능
              throw errorBodyAs(GithubServerException::class.java, response.body)
                  ?: IOException(response.message)
          }

          return response
      }

      companion object {

          val DOMAIN: String
              get() = "https://api.github.com"

          val headers: ArrayMap<String, String>
              get() = ArrayMap<String, String>().apply {
                  put("x-app-ver", BuildConfig.VERSION_NAME)
                  put("x-os-ver", Build.VERSION.RELEASE)
                  put("x-device-model", Build.MODEL)
              }
      }

  }
  ```
  
- 서비스 클래스 생성

  ```kotlin
  private val service by lazy { githubRetrofit.createService(GithubService::class.java) }
  ```
