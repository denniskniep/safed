package de.denniskniep.safed;

import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.report.ScanResultReport;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.mtls.scans.scanner.InvalidCAScanner;
import de.denniskniep.safed.mtls.scans.scanner.NoClientCertScanner;
import de.denniskniep.safed.oidc.scans.scanner.*;
import de.denniskniep.safed.saml.scans.scanner.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssessmentContainerTests extends ApplicationBaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(AssessmentContainerTests.class);
    private static final String ALL_SCANNERS = null;

    public AssessmentContainerTests() {
        super("8070");
    }

    private static String[] ArrayOf(String... values){
        return values;
    }

    private static List<Arguments> oidcTestCases() {
        var clientIds = List.of(
                EXAMPLE_OIDC_CODE_FLOW.clientId(),
                EXAMPLE_OIDC_HYBRID_FLOW.clientId(),
                EXAMPLE_OIDC_IMPLICIT_FLOW.clientId()
        );

        List<Arguments> allCases = new ArrayList<>();
        for (String clientId : clientIds) {
            var cases = List.of(
                    Arguments.of(
                            clientId,
                            ALL_SCANNERS,
                            ArrayOf(),
                            ScanResultStatus.OK
                    ),

                    Arguments.of(
                            clientId,
                            BreakSignatureOidcScanner.class.getSimpleName(),
                            ArrayOf(
                            ".*Invalid signature"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            clientId,
                            ExpiredBeforeIssuedOidcScanner.class.getSimpleName(),
                            ArrayOf(
                            ".*expiresAt must be after issuedAt"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            clientId,
                            ExpiredOidcScanner.class.getSimpleName(),
                            ArrayOf(
                        ".*Jwt expired at .*",
                                ".*The ID Token contains invalid claims: .*exp=.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            clientId,
                            FutureNotBeforeOidcScanner.class.getSimpleName(),
                            ArrayOf(
                            ".*Jwt used before .*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            clientId,
                            NoneAlgOidcScanner.class.getSimpleName(),
                            ArrayOf(
                            ".*Invalid serialized unsecured/JWS/JWE object: Missing second delimiter"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            clientId,
                            NoSignatureOidcScanner.class.getSimpleName(),
                            ArrayOf(
                            ".*Invalid serialized unsecured/JWS/JWE object: Missing second delimiter"
                            ),
                            ScanResultStatus.VULNERABLE
                    )
            );
            allCases.addAll(cases);
        }

        return allCases;
    }

    private static List<Arguments> samlTestCases() {
        return List.of(
                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            ALL_SCANNERS,
                            ArrayOf(),
                            ScanResultStatus.OK
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            BreakSignature.class.getSimpleName(),
                            ArrayOf(
                            ".*Invalid signature for object.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            Expired.class.getSimpleName(),
                            ArrayOf(
                                    ".*NotOnOrAfter condition of .* is no longer valid.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            ExpiredBeforeIssued.class.getSimpleName(),
                            ArrayOf(
                                    ".*NotOnOrAfter condition of .* is no longer valid.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            FutureNotBefore.class.getSimpleName(),
                            ArrayOf(
                                    ".*NotBefore condition of .* is not yet valid.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            NoSignature.class.getSimpleName(),
                            ArrayOf(
                                    ".*Either the response or one of the assertions is unsigned.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            OtherAudience.class.getSimpleName(),
                            ArrayOf(
                                    ".*None of the audiences within Assertion .* matched the list of valid audiances.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            OtherIssuer.class.getSimpleName(),
                            ArrayOf(
                                    ".*Invalid signature for object.*",
                                    ".*Invalid issuer .* for SAML response.*",
                                    ".*Issuer of Assertion .* did not match any valid issuers.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            ReplayFirstPositiveSamlResponse.class.getSimpleName(),
                            ArrayOf(
                                    ".*The InResponseTo attribute .* does not match the ID of the authentication request.*",
                                    ".*No subject confirmation methods were met for assertion.*",
                                    ".*RelayState in response: .* differs from sent RelayState:.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            OtherInResponseTo.class.getSimpleName(),
                            ArrayOf(
                                    ".*The InResponseTo attribute .* does not match the ID of the authentication request.*",
                                    ".*No subject confirmation methods were met for assertion.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            OtherRelayState.class.getSimpleName(),
                            ArrayOf(
                                    ".*RelayState in response: .* differs from sent RelayState:.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    ),

                    Arguments.of(
                            EXAMPLE_SAML.clientId(),
                            SuccessStatusIsFailed.class.getSimpleName(),
                            ArrayOf(
                                    ".*Invalid status.*AuthnFailed.*"
                            ),
                            ScanResultStatus.VULNERABLE
                    )
            );
    }

    private static List<Arguments> mtlsTestCases() {
        return List.of(
                Arguments.of(
                        EXAMPLE_MTLS.clientId(),
                        ALL_SCANNERS,
                        ArrayOf(),
                        ScanResultStatus.OK
                ),

                Arguments.of(
                        EXAMPLE_MTLS.clientId(),
                        NoClientCertScanner.class.getSimpleName(),
                        ArrayOf("No client certificate provided"),
                        ScanResultStatus.VULNERABLE
                ),

                Arguments.of(
                        EXAMPLE_MTLS.clientId(),
                        InvalidCAScanner.class.getSimpleName(),
                        ArrayOf(
                                "Invalid certificate signature: Certificate is not signed by the configured CA.*",
                                "Certificate issuer mismatch. Expected issuer: CN=RootCA,OU=CA,O=Company,L=City,ST=State,C=DE, Found: C=DE,ST=State,L=City,O=Company,OU=CA,CN=FakeRootCA"
                        ),
                        ScanResultStatus.VULNERABLE
                )
        );
    }

    private static Stream<Arguments> testCases() {
        return Stream.of(
            samlTestCases(),
            oidcTestCases(),
            mtlsTestCases()
        ).flatMap(Collection::stream);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String clientId, String triggeredScanner, String[] ignoredErrorDescriptions, ScanResultStatus expectedStatus) {
        LOG.info("Starting test with clientId={}, triggeredScanner={}", clientId, triggeredScanner);

        var triggeredScanners = triggeredScanner == null ? new ArrayList<String>() : List.of(triggeredScanner);
        ExampleApp exampleApp = findExampleApp(clientId);
        exampleApp.setIgnoredErrorDescriptions(ignoredErrorDescriptions);

        Report result = this.runAssessment(clientId, triggeredScanners);

        Assertions.assertNotNull(result);
        LOG.info(result.asJson());

        var lastSeenErrorDescriptions = exampleApp.getLastSeenErrorDescriptions();
        if(lastSeenErrorDescriptions.isEmpty()){
            LOG.info("No errors occurred in app (after ignoreFilter was applied)");
        }else{
            LOG.info("Following errors occurred in app (after ignoreFilter was applied): \n{}", String.join("\n", lastSeenErrorDescriptions));
        }

        Assertions.assertEquals(expectedStatus, result.getStatus());
        for (var scanner : triggeredScanners) {
            ScanResultReport finding = result.getFindings().get(scanner);
            Assertions.assertNotNull(finding, "Expected that scanner " + scanner + " found a vulnerability");
            Assertions.assertEquals(ScanResultStatus.VULNERABLE, finding.getStatus(), "Expected that scanner " + scanner + " found a vulnerability");
        }
        Assertions.assertEquals(triggeredScanners.size(), result.getFindings().size(), "Expected to find " + triggeredScanners.size() + " vulnerabilities, but it was " + result.getFindings().size());
    }
}
