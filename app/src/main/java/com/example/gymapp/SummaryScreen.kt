package com.example.gymapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.ui.theme.*

private val WarmupAmber = Color(0xFFF5A524)

@Composable
fun SummaryScreen(
    summary: WorkoutSummaryData,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    val expandedLog = remember { mutableStateMapOf<Int, Boolean>() }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding(),
        ) {
            // ── Hero checkmark ────────────────────────────────────────────
            var checkTarget by remember { mutableStateOf(0f) }
            LaunchedEffect(Unit) { checkTarget = 1f }
            val scale by animateFloatAsState(
                targetValue = checkTarget,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "checkmark",
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoodColor.copy(alpha = 0.16f), Color.Transparent),
                            radius = 600f,
                        )
                    )
                    .padding(vertical = 34.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .scale(scale)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF5FD97A), Color(0xFF3FB85F))),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Check, null, tint = BgColor, modifier = Modifier.size(46.dp))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("Workout complete!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColor)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${summary.title} · Great session, ${UserStore.displayName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SubTextColor,
                    )
                }
            }

            // ── Stats row ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, LineColor, RoundedCornerShape(18.dp))
                    .background(CardColor),
            ) {
                SummaryStat(Icons.Rounded.AccessTime, summary.dur,        "Duration",   MuscleBack,    Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(70.dp).background(LineColor).align(Alignment.CenterVertically))
                SummaryStat(Icons.Rounded.Layers,     "${summary.sets}",  "Sets",       AccentColor,   Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(70.dp).background(LineColor).align(Alignment.CenterVertically))
                SummaryStat(Icons.Rounded.FitnessCenter, summary.vol,     "kg Volume",  GoodColor,     Modifier.weight(1f))
            }

            // ── Steps / calories burned, from a paired watch's sensors ─────
            val summarySteps = summary.steps
            val summaryCalories = summary.calories
            if (summarySteps != null || summaryCalories != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, LineColor, RoundedCornerShape(18.dp))
                        .background(CardColor),
                ) {
                    if (summarySteps != null) {
                        SummaryStat(Icons.Rounded.DirectionsWalk, "$summarySteps", "Steps", MuscleBack, Modifier.weight(1f))
                    }
                    if (summarySteps != null && summaryCalories != null) {
                        Box(modifier = Modifier.width(1.dp).height(70.dp).background(LineColor).align(Alignment.CenterVertically))
                    }
                    if (summaryCalories != null) {
                        SummaryStat(Icons.Rounded.LocalFireDepartment, "${summaryCalories.toInt()}", "kcal Burned", WarmupAmber, Modifier.weight(1f))
                    }
                }
            }

            // ── Heart rate over time ────────────────────────────────────────
            if (summary.hrSamples.size >= 2) {
                Spacer(Modifier.height(22.dp))
                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    val bpms = summary.hrSamples.map { it.second }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Favorite, null, tint = MuscleChest, modifier = Modifier.size(18.dp))
                            Text("Heart Rate", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextColor)
                        }
                        Text(
                            "avg ${bpms.average().toInt()} · min ${bpms.min()} · max ${bpms.max()} bpm",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SubTextColor,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    HeartRateGraph(
                        samples = summary.hrSamples,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, LineColor, RoundedCornerShape(16.dp))
                            .background(CardColor)
                            .padding(12.dp),
                    )
                }
            }

            if (summary.prs.isNotEmpty()) {
            Spacer(Modifier.height(22.dp))

            // ── New PRs ───────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.EmojiEvents, null, tint = MuscleCore, modifier = Modifier.size(18.dp))
                    Text("New PRs", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextColor)
                    Box(
                        modifier = Modifier
                            .background(MuscleCore.copy(alpha = 0.12f), RoundedCornerShape(99.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("${summary.prs.size}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MuscleCore)
                    }
                }

                Spacer(Modifier.height(12.dp))

                summary.prs.forEach { pr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(MuscleCore.copy(alpha = 0.08f), CardColor.copy(alpha = 0.5f)))
                            )
                            .border(1.dp, MuscleCore.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(MuscleCore.copy(alpha = 0.13f), RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.EmojiEvents, null, tint = MuscleCore, modifier = Modifier.size(19.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pr.name, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextColor)
                            Text(pr.detail, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = SubTextColor)
                        }
                        Text(pr.delta, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoodColor)
                    }
                }
            }
            }

            Spacer(Modifier.height(22.dp))

            // ── Workout log ───────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text("Workout log", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextColor)
                Spacer(Modifier.height(12.dp))

                summary.log.forEachIndexed { i, ex ->
                    val isOpen = expandedLog[i] == true
                    val col = muscleColor(ex.group)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, LineColor, RoundedCornerShape(14.dp))
                            .background(CardColor),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedLog[i] = !isOpen }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                        ) {
                            Box(modifier = Modifier.size(9.dp).background(col, CircleShape))
                            Text(ex.name, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextColor, modifier = Modifier.weight(1f))
                            Text("${ex.sets.size} sets", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MutedColor)
                            Icon(
                                if (isOpen) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.ChevronRight,
                                null,
                                tint = MutedColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        if (isOpen) {
                            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp)) {
                                ex.sets.forEachIndexed { si, setStr ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(SubtleFillColor, RoundedCornerShape(7.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text("${si + 1}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SubTextColor)
                                        }
                                        Text(
                                            "$setStr kg × reps",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextColor,
                                        )
                                    }
                                    if (si < ex.sets.size - 1) {
                                        HorizontalDivider(color = LineColor, thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // ── Sticky action buttons ─────────────────────────────────────────
        HorizontalDivider(color = LineColor, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgColor)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { showDiscardConfirm = true },
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SubTextColor),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Icon(Icons.Rounded.DeleteOutline, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Discard", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text("Save Workout", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            containerColor = CardElevColor,
            titleContentColor = TextColor,
            textContentColor = SubTextColor,
            title = { Text("Discard workout?", fontWeight = FontWeight.Bold) },
            text = { Text("This workout will not be saved. Are you sure you want to discard it?") },
            confirmButton = {
                TextButton(onClick = { showDiscardConfirm = false; onDiscard() }) {
                    Text("Discard", color = MuscleChest, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Cancel", color = SubTextColor, fontWeight = FontWeight.SemiBold)
                }
            },
        )
    }
}

/** Simple line graph of heart rate over the workout's elapsed time. */
@Composable
private fun HeartRateGraph(samples: List<Pair<Int, Int>>, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minBpm = samples.minOf { it.second }
        val maxBpm = samples.maxOf { it.second }
        val range = (maxBpm - minBpm).coerceAtLeast(1)
        val minSec = samples.first().first
        val maxSec = samples.last().first
        val span = (maxSec - minSec).coerceAtLeast(1)

        val pts = samples.map { (sec, bpm) ->
            androidx.compose.ui.geometry.Offset(
                x = (sec - minSec).toFloat() / span * w,
                y = h - (bpm - minBpm).toFloat() / range * h,
            )
        }

        val linePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
        }
        val areaPath = androidx.compose.ui.graphics.Path().apply {
            addPath(linePath)
            lineTo(pts.last().x, h)
            lineTo(pts.first().x, h)
            close()
        }
        drawPath(
            areaPath,
            Brush.verticalGradient(colors = listOf(MuscleChest.copy(alpha = 0.25f), Color.Transparent), startY = 0f, endY = h),
        )
        drawPath(
            linePath,
            color = MuscleChest,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.6f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun SummaryStat(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(19.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextColor)
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MutedColor)
    }
}
