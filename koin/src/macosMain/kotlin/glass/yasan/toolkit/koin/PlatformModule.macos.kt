package glass.yasan.toolkit.koin

import glass.yasan.toolkit.core.url.UrlLauncher
import glass.yasan.toolkit.core.url.UrlLauncherImpl
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    factory<UrlLauncher> { UrlLauncherImpl(toolkitApp = get()) }
}
