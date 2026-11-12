package glass.yasan.toolkit.core.url

import glass.yasan.toolkit.core.annotation.InternalToolkitApi
import glass.yasan.toolkit.core.app.ToolkitApp
import glass.yasan.toolkit.core.tracking.withUtmSourceParameter
import glass.yasan.toolkit.core.url.UrlLaunchResult.Failure.Error
import glass.yasan.toolkit.core.url.UrlLaunchResult.Failure.InvalidUrl
import glass.yasan.toolkit.core.url.UrlLaunchResult.Failure.Unsupported
import glass.yasan.toolkit.core.url.UrlLaunchResult.Success
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

public actual class UrlLauncherImpl(
    private val toolkitApp: ToolkitApp,
) : UrlLauncher {

    @OptIn(InternalToolkitApi::class)
    actual override suspend fun launch(url: String): UrlLaunchResult =
        try {
            val trackedUrl = url.withUtmSourceParameter(toolkitApp)
                ?: return InvalidUrl
            val nsUrl = NSURL.URLWithString(
                URLString = trackedUrl.toString(),
            ) ?: return InvalidUrl

            if (NSWorkspace.sharedWorkspace.openURL(nsUrl)) {
                Success
            } else {
                Unsupported
            }
        } catch (e: Exception) {
            Error(e)
        }
}
