package com.userstar.oqrticket.timber

import timber.log.Timber

open class ThreadIncludedDebugTree : Timber.DebugTree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        var newTag = ""
        if (tag != null) {
            newTag = "<${Thread.currentThread().name}> $tag"
        }
        super.log(priority, newTag, message, t)
    }

    override fun createStackElementTag(element: StackTraceElement): String? {
        return "${super.createStackElementTag(element)}(${element.lineNumber})"
    }
}