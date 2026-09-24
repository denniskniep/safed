package de.denniskniep.safed.common.auth.browser;

import de.denniskniep.safed.common.scans.Page;

public record InitializationResult<T>(T result, Page page) {
}
