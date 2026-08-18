package com.github.alirezakeshavarzd.chrono.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.concurrency.AppExecutorUtil
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class GlobalTimeTracker {

    private var activeProject: Project? = null
    private var activeStartTime: LocalDateTime? = null
//    private var heartbeatTask: ScheduledFuture<*>? = null

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val logDir = File(System.getProperty("user.home"), ".jetbrains-chrono")

    companion object {
        val instance: GlobalTimeTracker
            get() = service()
    }

    @Synchronized
    fun isProjectActive(project: Project): Boolean {
        return activeProject == project
    }

    @Synchronized
    fun selectProjectToTrack(project: Project) {
        if (activeProject == project) return

        if (activeProject != null) {
            flushActiveSession(isClosing = true)
        }

        activeProject = project
        activeStartTime = LocalDateTime.now()

        refreshAllStatusBarWidgets()
        println("[Chrono] Tracking paused for ${project.name}")
    }

    @Synchronized
    fun pauseTracking(project: Project) {
        if (activeProject == project) {
            flushActiveSession(isClosing = true)
            activeProject = null
            activeStartTime = null
            refreshAllStatusBarWidgets()
            println("[Chrono] Tracking paused for ${project.name}")
        }
    }

    @Synchronized
    fun onProjectClosed(project: Project) {
        if (activeProject == project) {
            pauseTracking(project)
        }
    }

    private fun refreshAllStatusBarWidgets() {
        for (openProject in ProjectManager.getInstance().openProjects) {
            val statusBar = WindowManager.getInstance().getStatusBar(openProject)
            statusBar?.updateWidget("ChronoStatusBarWidget")
        }
    }

    private fun flushActiveSession(isClosing: Boolean) {
        val currentProject = activeProject ?: return
        val start = activeStartTime ?: return
        val now = LocalDateTime.now()

        val durationMinutes = Duration.between(start, now).toMinutes()
        if (durationMinutes < 1 && !isClosing) return

        val todayFile = File(logDir, "session_log_${LocalDate.now()}.csv")
        ensureCsvFileExists(todayFile)

        val formattedStart = start.format(timeFormatter)
        val formattedEnd = now.format(timeFormatter)
        val safeProjectName = "\"${currentProject.name.replace("\"", "\"\"")}\""

        val csvRow = "$safeProjectName,$formattedStart,$formattedEnd,$durationMinutes\n"
        todayFile.appendText(csvRow)

        if (isClosing) {
            println("[Chrono] Closed session for ${currentProject.name}: $durationMinutes min")
        } else {
            activeStartTime = now
            println("[Chrono] Heartbeat auto-saved $durationMinutes min for ${currentProject.name}")
        }
    }

    private fun ensureCsvFileExists(file: File) {
        if (!logDir.exists()) logDir.mkdirs()
        if (!file.exists()) {
            file.writeText("ProjectName, StartTime, EndTime, Duration(Min)\n")
        }
    }
}