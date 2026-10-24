package glass.yasan.toolkit.core.tracking

import glass.yasan.toolkit.core.annotation.InternalToolkitApi
import glass.yasan.toolkit.core.app.ToolkitApp

/**
 * [url] with this app's `utm_source` if it is a web link, for links shown rather than opened, such as a QR code.
 */
@OptIn(InternalToolkitApi::class)
public fun ToolkitApp.trackedUrl(url: String): String =
    url.withUtmSourceParameter(this)?.toString() ?: url
