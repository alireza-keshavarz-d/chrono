package com.github.alirezakeshavarzd.chrono.listeners

import com.github.alirezakeshavarzd.chrono.services.GlobalTimeTracker
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class ProjectCloseListener : ProjectManagerListener {
    override fun projectClosing(project: Project) {
        GlobalTimeTracker.instance.onProjectClosed(project)
    }
}