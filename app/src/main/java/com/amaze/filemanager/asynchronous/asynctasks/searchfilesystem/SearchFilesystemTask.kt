package com.amaze.filemanager.asynchronous.asynctasks.searchfilesystem

import android.app.Activity
import android.util.Log
import androidx.annotation.MainThread
import androidx.preference.PreferenceManager
import com.amaze.filemanager.asynchronous.asynctasks.PublisherTask
import com.amaze.filemanager.asynchronous.asynctasks.Task
import com.amaze.filemanager.file_operations.filesystem.OpenMode
import com.amaze.filemanager.filesystem.HybridFile
import com.amaze.filemanager.filesystem.HybridFileParcelable
import com.amaze.filemanager.ui.fragments.SearchWorkerFragment.HelperCallbacks
import com.amaze.filemanager.ui.fragments.preference_fragments.PreferencesConstants
import com.amaze.filemanager.utils.OnFileFound
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.util.concurrent.Callable
import java.util.regex.Pattern

class SearchFilesystemTask(
    a: Activity,
    input: String,
    openMode: OpenMode?,
    rootMode: Boolean,
    isRegexEnabled: Boolean,
    isMatchesEnabled: Boolean,
    path: String
): PublisherTask<HybridFileParcelable, SearchFilesystemPublish> {
    private val task = SearchFilesystemPublish(a, input, openMode, rootMode, isRegexEnabled, isMatchesEnabled, path)

    override fun getTask() = task

    override fun onError(error: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onNext(value: HybridFileParcelable) {
        TODO("Not yet implemented")
    }

    override fun onFinish() {
        TODO("Not yet implemented")
    }

}

class SearchFilesystemPublish(
    a: Activity,
    private val input: String,
    private val openMode: OpenMode?,
    private val rootMode: Boolean,
    private val isRegexEnabled: Boolean,
    private val isMatchesEnabled: Boolean,
    val path: String
): Publisher<HybridFileParcelable> {
    private var activity = WeakReference(a)


    override fun subscribe(s: Subscriber<in HybridFileParcelable>) {
        val file = HybridFile(openMode, path)
        file.generateMode(activity.get())
        if (file.isSmb) return null

        // level 1
        // if regex or not

        // level 1
        // if regex or not
        if (!isRegexEnabled) {
            search(s, file, input)
        } else {
            // compile the regular expression in the input
            val pattern = Pattern.compile(bashRegexToJava(input))
            // level 2
            if (!isMatchesEnabled) {
                searchRegExFind(s, file, pattern)
            } else {
                searchRegExMatch(s, file, pattern)
            }
        }

        s.onComplete()
    }

    /**
     * Recursively search for occurrences of a given text in file names and publish the result
     *
     * @param directory the current path
     */
    private fun search(s: Subscriber<in HybridFileParcelable>, directory: HybridFile, filter: SearchFilter) {
        if (!directory.isDirectory(activity.get())) {// do you have permission to read this directory?
            Log.d(TAG, "Cannot search " + directory.path + ": Permission Denied")
            return
        }

        directory.forEachChildrenFile(
            activity.get(),
            rootMode,
            object : OnFileFound {
                override fun onFileFound(file: HybridFileParcelable) {
                    val showHiddenFiles =
                        PreferenceManager.getDefaultSharedPreferences(activity.get())
                            .getBoolean(PreferencesConstants.PREFERENCE_SHOW_HIDDENFILES, false)
                    if (showHiddenFiles || !file.isHidden) {
                        if (filter.searchFilter(file.getName(activity.get()))) {
                            s.onNext(file)
                        }
                        if (file.isDirectory) {
                            search(s, file, filter)
                        }
                    }
                }
            })
        }

    /**
     * Recursively search for occurrences of a given text in file names and publish the result
     *
     * @param file the current path
     * @param query the searched text
     */
    private fun search(s: Subscriber<in HybridFileParcelable>, file: HybridFile, query: String) {
        search(s, file, object : SearchFilter {
            override fun searchFilter(fileName: String): Boolean {
                return fileName.toLowerCase().contains(query.toLowerCase())
            }
        })
    }

    /**
     * Recursively find a java regex pattern [Pattern] in the file names and publish the result
     *
     * @param file the current file
     * @param pattern the compiled java regex
     */
    private fun searchRegExFind(s: Subscriber<in HybridFileParcelable>, file: HybridFile, pattern: Pattern) {
        search(s, file, object : SearchFilter {
            override fun searchFilter(fileName: String): Boolean {
                return pattern.matcher(fileName).find()
            }
        })
    }

    /**
     * Recursively match a java regex pattern [Pattern] with the file names and publish the
     * result
     *
     * @param file the current file
     * @param pattern the compiled java regex
     */
    private fun searchRegExMatch(s: Subscriber<in HybridFileParcelable>, file: HybridFile, pattern: Pattern) {
        search(s, file, object : SearchFilter {
            override fun searchFilter(fileName: String): Boolean {
                return pattern.matcher(fileName).matches()
            }
        })
    }

    /**
     * method converts bash style regular expression to java. See [Pattern]
     *
     * @return converted string
     */
    private fun bashRegexToJava(originalString: String): String {
        val stringBuilder = StringBuilder()
        for (i in 0 until originalString.length) {
            when (originalString[i].toString() + "") {
                "*" -> stringBuilder.append("\\w*")
                "?" -> stringBuilder.append("\\w")
                else -> stringBuilder.append(originalString[i])
            }
        }
        Log.d(javaClass.simpleName, stringBuilder.toString())
        return stringBuilder.toString()
    }

    interface SearchFilter {
        fun searchFilter(fileName: String): Boolean
    }
}