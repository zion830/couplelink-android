package com.desertfox.couplelink.network

import android.content.Context
import com.desertfox.couplelink.util.ACCESS_TOKEN
import com.desertfox.couplelink.util.sharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitProvider constructor(private val context: Context) {

    val coupleLinkApi: Api by lazy {
        Retrofit.Builder()
            .baseUrl(StompUrl.BASE_URL)
            .client(provideOkHttpClient(provideLoggingInterceptor()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
    }

    private fun provideOkHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
        val b = OkHttpClient.Builder()
        b.addInterceptor { chain ->
            val accessToken = context.sharedPreferences().getString(ACCESS_TOKEN, "").orEmpty()
            return@addInterceptor chain.proceed(chain.request().newBuilder().let {
                it.header("Authorization", "Bearer $accessToken")
                it.build()
            })
        }
        b.addInterceptor(interceptor)
        return b.build()
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return interceptor
    }
}