package com.github.alirezakeshavarzd.chrono.ui

import com.github.alirezakeshavarzd.chrono.services.GlobalTimeTracker
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import java.awt.Component
import java.awt.event.MouseEvent
import com.intellij.util.Consumer

class TimeTrackerWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "ChronoStatusBarWidget"
    override fun getDisplayName(): String ="Chrono"
    override fun isAvailable(project: Project): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = ChronoWidget(project)
    override fun disposeWidget(widget: StatusBarWidget) {}
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

}

class ChronoWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {
    override fun ID(): String = "ChronoStatusBarWidget"
    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String {
        val isActive = GlobalTimeTracker.instance.isProjectActive(project)
        return if (isActive) "Tracking: ACTIVE" else "Tracking: PAUSED"
    }

    override fun getTooltipText(): String {
        return if(GlobalTimeTracker.instance.isProjectActive(project))
            "Click to pause tracking for this project"
        else
            "Click to set this project as the active tracked project"
    }

    override fun getAlignment(): Float = Component.CENTER_ALIGNMENT

    override fun getClickConsumer(): Consumer<MouseEvent> {
        return Consumer {
            val tracker = GlobalTimeTracker.instance
            if (tracker.isProjectActive(project)) {
                tracker.pauseTracking(project)
            } else {
                tracker.selectProjectToTrack(project)
            }
        }
    }

    override fun install(statusBar: StatusBar) {}
    override fun dispose() {}
}
