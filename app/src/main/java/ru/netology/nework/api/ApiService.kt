package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.model.AuthModel

interface ApiService {

    //post
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

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
    suspend fun savePost(@Header("auth") auth: String, @Body post: Post): Response<Post>

    @POST("posts/{post_id}/likes ")
    suspend fun likePostById(@Header("auth") auth: String, @Path("post_id") id: Int): Response<Post>

    @DELETE("posts/{post_id}/likes ")
    suspend fun unlikePostById(@Header("auth") auth: String, @Path("post_id") id: Int): Response<Post>

    //events
    @GET("events")
    suspend fun getAllEvent(): Response<List<Event>>

    @GET("events/latest")
    suspend fun getLatestEvent(@Query("count") count: Int): Response<List<Event>>

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

    @POST("events")
    suspend fun saveEvent(@Header("auth") auth: String, @Body event: Event): Response<Event>

    @POST("events/{event_id}/likes ")
    suspend fun likeEventById(@Header("auth") auth: String, @Path("event_id") id: Int): Response<Event>

    @DELETE("events/{event_id}/likes ")
    suspend fun unlikeEventById(@Header("auth") auth: String, @Path("event_id") id: Int): Response<Event>

    //job
    @GET("my/jobs/")
    suspend fun getMyJobs(@Header("auth") auth: String): Response<List<Job>>

    @GET("{user_id}/jobs/")
    suspend fun getJobsById(@Path("user_id") id: String): Response<List<Job>>

    @POST("my/jobs/")
    suspend fun saveJob(@Header("auth") auth: String, @Body job: Job): Response<Job>

    @DELETE("my/jobs/{job_id}/")
    suspend fun removeJob(@Header("auth") auth: String, @Path("job_id") id: String): Response<Unit>

    //user

    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("users/{user_id}")
    suspend fun getUserById(@Path("id") id: Int): Response<User>

    @FormUrlEncoded
    @POST("users/authentication/")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("password") password: String
    ): Response<AuthModel>

    @FormUrlEncoded
    @POST("users/registration/")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("name") name: String,
    ): Response<AuthModel>

    @Multipart
    @POST("users/registration")
    suspend fun registerWithPhoto(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part,
    ): Response<AuthModel>
}