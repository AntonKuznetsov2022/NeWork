package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post

interface ApiService {

    //post
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count : Int): Response<List<Post>>

    @GET("posts/{post_id}/before")
    suspend fun getBefore(
        @Path("post_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("posts/{post_id}/after")
    suspend fun getAfter(
        @Path("post_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Post>>


    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    //events
    @GET("events")
    suspend fun getAllEvent(): Response<List<Event>>

    @GET("events/latest")
    suspend fun getLatestEvent(@Query("count") count : Int): Response<List<Event>>

    @GET("events/{event_id}/before")
    suspend fun getBeforeEvent(
        @Path("event_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("events/{event_id}/after")
    suspend fun getAfterEvent(
        @Path("event_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Event>>
}