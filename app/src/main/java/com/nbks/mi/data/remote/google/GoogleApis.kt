package com.nbks.mi.data.remote.google

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

// ─────────────────────────────────────────────
// Google Calendar REST API
// ─────────────────────────────────────────────
interface GoogleCalendarApi {
    @GET("calendar/v3/calendars/primary/events")
    suspend fun listEvents(
        @Header("Authorization") authHeader: String,
        @Query("timeMin") timeMin: String,
        @Query("timeMax") timeMax: String,
        @Query("singleEvents") singleEvents: Boolean = true,
        @Query("orderBy") orderBy: String = "startTime",
        @Query("maxResults") maxResults: Int = 50,
    ): CalendarEventsResponse

    @GET("calendar/v3/users/me/calendarList")
    suspend fun listCalendars(
        @Header("Authorization") authHeader: String,
    ): CalendarListResponse
}

data class CalendarEventsResponse(
    val items: List<GoogleCalendarEvent>?,
)

data class CalendarListResponse(
    val items: List<GoogleCalendarItem>?,
)

data class GoogleCalendarItem(
    val id: String,
    val summary: String?,
    val backgroundColor: String?,
)

data class GoogleCalendarEvent(
    val id: String,
    val summary: String?,
    val description: String?,
    val start: EventDateTime?,
    val end: EventDateTime?,
    val location: String?,
    @SerializedName("colorId") val colorId: String?,
)

data class EventDateTime(
    @SerializedName("dateTime") val dateTime: String?,
    @SerializedName("date") val date: String?,
)

// ─────────────────────────────────────────────
// Google Tasks REST API
// ─────────────────────────────────────────────
interface GoogleTasksApi {
    @GET("tasks/v1/users/@me/lists")
    suspend fun listTaskLists(
        @Header("Authorization") authHeader: String,
    ): TaskListsResponse

    @GET("tasks/v1/lists/{listId}/tasks")
    suspend fun listTasks(
        @Header("Authorization") authHeader: String,
        @Path("listId") listId: String,
        @Query("showCompleted") showCompleted: Boolean = false,
        @Query("showDeleted") showDeleted: Boolean = false,
    ): TasksResponse

    @PATCH("tasks/v1/lists/{listId}/tasks/{taskId}")
    suspend fun updateTask(
        @Header("Authorization") authHeader: String,
        @Path("listId") listId: String,
        @Path("taskId") taskId: String,
        @Body update: TaskUpdate,
    ): GoogleTask
}

data class TaskListsResponse(
    val items: List<GoogleTaskList>?,
)

data class GoogleTaskList(
    val id: String,
    val title: String?,
)

data class TasksResponse(
    val items: List<GoogleTask>?,
)

data class GoogleTask(
    val id: String,
    val title: String?,
    val notes: String?,
    val due: String?,
    val status: String?,  // "needsAction" or "completed"
)

data class TaskUpdate(
    val status: String?,
    val title: String? = null,
    val notes: String? = null,
)
