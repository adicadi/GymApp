package com.example.gymapp

// ── Models ──────────────────────────────────────────────────────────────────
// Plain data shared between the phone app and the Wear OS companion — no
// Compose / Android-framework dependency, so both sides can exchange these
// over the Wearable Data Layer using the same shapes the phone persists.

data class ExerciseLibraryItem(val name: String, val group: String, val equip: String)

data class WorkoutTemplate(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val exercises: List<ExerciseLibraryItem>,
) {
    val groups: List<String> get() = exercises.map { it.group }.distinct()
    val count: Int get() = exercises.size
}

/** Logged set classification. Warmup sets are excluded from volume, set counts and PRs. */
enum class SetType { WARMUP, NORMAL, FAILURE }

data class SetData(
    val prev: String,
    val weight: String,
    val reps: String,
    val done: Boolean,
    val type: SetType = SetType.NORMAL,
)

data class WorkoutExercise(
    val id: String,
    val name: String,
    val group: String,
    val equip: String,
    val open: Boolean = true,
    val sets: List<SetData> = listOf(SetData("—", "", "", false)),
)

/** A completed, persisted workout. */
data class SavedWorkout(
    val id: Long,
    val title: String,
    val dateMillis: Long,
    val durationSec: Int,
    val totalVolumeKg: Double,
    val totalSets: Int,
    val exercises: List<WorkoutExercise>,
    val avgHeartRate: Int? = null,
) {
    val muscleGroups: List<String> get() = exercises.map { it.group }.distinct()
}

data class PrItem(val name: String, val detail: String, val delta: String)
data class LogExercise(val name: String, val group: String, val sets: List<String>)
data class WorkoutSummaryData(
    val title: String,
    val dur: String,
    val sets: Int,
    val vol: String,
    val prs: List<PrItem>,
    val log: List<LogExercise>,
    /** Steps taken during the session, from the watch's sensors — null if no watch is paired. */
    val steps: Long? = null,
    /** Active calories burned during the session, from the watch's sensors — null if no watch is paired. */
    val calories: Double? = null,
    /** (elapsedSec, bpm) heart-rate samples taken across the session, for the post-workout graph. */
    val hrSamples: List<Pair<Int, Int>> = emptyList(),
)

/** Today's dashboard stats. */
data class TodayStats(
    val steps: String,
    val calories: String,
    val heartRate: String,
)
