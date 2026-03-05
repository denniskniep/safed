package de.denniskniep.safed.mtls.scans;

import de.denniskniep.safed.common.auth.browser.HttpRequest;
import de.denniskniep.safed.common.scans.Scanner;

public interface MtlsScanner extends Scanner {

    HttpRequest beforeRequest(HttpRequest get);
}
