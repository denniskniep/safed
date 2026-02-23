package de.denniskniep.safed;

import de.denniskniep.safed.common.report.ScanResultReport;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.oidc.OidcAssessment;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import de.denniskniep.safed.oidc.config.OidcConfig;
import de.denniskniep.safed.saml.SamlAssessment;
import de.denniskniep.safed.saml.config.SamlClientConfig;
import de.denniskniep.safed.saml.config.SamlConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest(classes = {SafedApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssessmentTests extends ApplicationBaseTest {

	@Autowired
	private OidcAssessment oidcAssessment;

	@Autowired
	private OidcConfig oidcConfig;

	@Autowired
	private SamlAssessment samlAssessment;

	@Autowired
	private SamlConfig samlConfig;

	public AssessmentTests(@LocalServerPort int port) {
		super(Integer.toString(port));
	}

	@BeforeEach
	protected void testSetUp() {
		exampleOidcCodeFlowApp.setIgnoredErrorDescriptions(new String[]{});
		exampleOidcHybridFlowApp.setIgnoredErrorDescriptions(new String[]{});
		exampleOidcImplicitFlowApp.setIgnoredErrorDescriptions(new String[]{});
		exampleSamlApp.setIgnoredErrorDescriptions(new String[]{});
	}

	private void oidcTest(String clientId, String[] triggeredScanners, String[] ignoredErrorDescriptions){
		exampleOidcCodeFlowApp.setIgnoredErrorDescriptions(ignoredErrorDescriptions);
		exampleOidcHybridFlowApp.setIgnoredErrorDescriptions(ignoredErrorDescriptions);
		exampleOidcImplicitFlowApp.setIgnoredErrorDescriptions(ignoredErrorDescriptions);

		OidcClientConfig oidcClientConfig = (OidcClientConfig)oidcConfig.getClient(clientId).deepCopy();
		oidcClientConfig.setScanners(Arrays.asList(triggeredScanners));
		var result = oidcAssessment.run(oidcClientConfig);

		LOG.info(result.asJson());

		List<String> lastSeenErrorDescriptions = new ArrayList<>();
		lastSeenErrorDescriptions.addAll(exampleOidcCodeFlowApp.getLastSeenErrorDescriptions());
		lastSeenErrorDescriptions.addAll(exampleOidcHybridFlowApp.getLastSeenErrorDescriptions());
		lastSeenErrorDescriptions.addAll(exampleOidcImplicitFlowApp.getLastSeenErrorDescriptions());
		if(lastSeenErrorDescriptions.isEmpty()){
			LOG.info("No errors occurred in app (after ignoreFilter was applied)");
		}else{
			LOG.info("Following errors occurred in app (after ignoreFilter was applied): \n{}", String.join("\n", lastSeenErrorDescriptions));
		}

		Assertions.assertEquals(ScanResultStatus.VULNERABLE, result.getStatus());
		for (var scanner : triggeredScanners) {
			ScanResultReport finding = result.getFindings().get(scanner);
			Assertions.assertNotNull(finding, "Expected that scanner " + scanner + " found a vulnerability");
			Assertions.assertEquals(ScanResultStatus.VULNERABLE,finding.getStatus(), "Expected that scanner " + scanner + " found a vulnerability");
		}
		Assertions.assertEquals(triggeredScanners.length, result.getFindings().size(), "Expected that " + triggeredScanners.length + " scanners find vulnerabilities, but it was " + result.getFindings().size());
	}

	private static Stream<Arguments> oidcClientIds() {
		return Stream.of(
				Arguments.of(EXAMPLE_OIDC_CODE_FLOW.clientId()),
				Arguments.of(EXAMPLE_OIDC_HYBRID_FLOW.clientId()),
				Arguments.of(EXAMPLE_OIDC_IMPLICIT_FLOW.clientId())
		);
	}


	@ParameterizedTest
	@MethodSource("oidcClientIds")
	void OidcTest_NoVulnerabilities(String clientId) {
		OidcClientConfig oidcClientConfig = (OidcClientConfig)oidcConfig.getClient(clientId).deepCopy();
		var result = oidcAssessment.run(oidcClientConfig);
		LOG.info(result.asJson());

		Assertions.assertEquals(ScanResultStatus.OK, result.getStatus());
		Assertions.assertEquals(0, result.getFindings().size());
	}

	@ParameterizedTest
	@MethodSource("oidcClientIds")
	void OidcTest_BreakSignature_Scanner(String clientId) {
		oidcTest(
				clientId,
				new String[]{
					"BreakSignatureOidcScanner"
				},
				new String[]{
					".*Invalid signature"
				}
			);
	}

	@ParameterizedTest
	@MethodSource("oidcClientIds")
	void OidcTest_ExpiredBeforeIssue_Scanner(String clientId) {
		oidcTest(
				clientId,
				new String[]{
					"ExpiredBeforeIssuedOidcScanner"
				},
				new String[]{
					".*expiresAt must be after issuedAt"
				}
		);
	}

	@ParameterizedTest
	@MethodSource("oidcClientIds")
	void OidcTest_Expired_Scanner(String clientId) {
		oidcTest(
				clientId,
				new String[]{
					"ExpiredOidcScanner"
				},
				new String[]{
						".*Jwt expired at .*",
						".*The ID Token contains invalid claims: .*exp=.*"
				}
		);
	}

	@ParameterizedTest
	@MethodSource("oidcClientIds")
	void OidcTest_FutureNotBefore_Scanner(String clientId) {
		oidcTest(
				clientId,
				new String[]{
						"FutureNotBeforeOidcScanner"
				},
				new String[]{
						".*Jwt used before .*"
				}
		);
	}

	@ParameterizedTest
	@MethodSource("oidcClientIds")
	void OidcTest_NoneAlgAndNoSignature_Scanner(String clientId) {
		oidcTest(
				clientId,
				new String[]{
					"NoneAlgOidcScanner",
				},
				new String[]{
					".*Invalid serialized unsecured/JWS/JWE object: Missing second delimiter",
				}
		);
	}

	@ParameterizedTest
	@MethodSource("oidcClientIds")
	void OidcTest_NoSignatureOidcScanner_Scanner(String clientId) {
		oidcTest(
				clientId,
				new String[]{
						"NoSignatureOidcScanner"
				},
				new String[]{
						".*Invalid serialized unsecured/JWS/JWE object: Missing second delimiter"
				}
		);
	}

	private void samlTest(String[] triggeredScanners, String[] ignoredSamlErrorDescriptions){
		exampleSamlApp.setIgnoredErrorDescriptions(ignoredSamlErrorDescriptions);

		SamlClientConfig samlClientConfig = samlConfig.getClient(EXAMPLE_SAML_CLIENT.clientId()).deepCopy();
		samlClientConfig.setScanners(Arrays.asList(triggeredScanners));
		var result = samlAssessment.run(samlClientConfig);

		LOG.info(result.asJson());

		List<String> lastSeenErrorDescriptions = exampleSamlApp.getLastSeenErrorDescriptions();
		if(lastSeenErrorDescriptions.isEmpty()){
			LOG.info("No errors occurred in app (after ignoreFilter was applied)");
		}else{
			LOG.info("Following errors occurred in app (after ignoreFilter was applied): \n{}", String.join("\n", lastSeenErrorDescriptions));
		}

		Assertions.assertEquals(ScanResultStatus.VULNERABLE, result.getStatus());
		for (var scanner : triggeredScanners) {
			ScanResultReport finding = result.getFindings().get(scanner);
			Assertions.assertNotNull(finding, "Expected that scanner " + scanner + " found a vulnerability");
			Assertions.assertEquals(ScanResultStatus.VULNERABLE,finding.getStatus(), "Expected that scanner " + scanner + " found a vulnerability");
		}
		Assertions.assertEquals(triggeredScanners.length, result.getFindings().size(), "Expected that " + triggeredScanners.length + " scanners find vulnerabilities, but it was " + result.getFindings().size());
	}


	@Test
	void SamlTest_NoVulnerabilities() {
		SamlClientConfig samlClientConfig = samlConfig.getClient(EXAMPLE_SAML_CLIENT.clientId()).deepCopy();
		var result = samlAssessment.run(samlClientConfig);
		LOG.info(result.asJson());

		Assertions.assertEquals(ScanResultStatus.OK, result.getStatus());
		Assertions.assertEquals(0, result.getFindings().size());
	}

	@Test
	void SamlTest_BreakSignature_Scanner() {
		samlTest(
				new String[]{
						"BreakSignature"
				},
				new String[]{
						".*Invalid signature for object.*"
				}
		);
	}

	@Test
	void SamlTest_Expired_Scanner() {
		samlTest(
				new String[]{
						"Expired",
				},
				new String[]{
						".*NotOnOrAfter condition of .* is no longer valid.*"
				}
		);
	}

	@Test
	void SamlTest_ExpiredBeforeIssued_Scanner() {
		samlTest(
				new String[]{
						"ExpiredBeforeIssued"
				},
				new String[]{
						".*NotOnOrAfter condition of .* is no longer valid.*"
				}
		);
	}

	@Test
	void SamlTest_FutureNotBefore_Scanner() {
		samlTest(
				new String[]{
						"FutureNotBefore"
				},
				new String[]{
						".*NotBefore condition of .* is not yet valid.*"
				}
		);
	}

	@Test
	void SamlTest_NoSignature_Scanner() {
		samlTest(
				new String[]{
						"NoSignature"
				},
				new String[]{
						".*Either the response or one of the assertions is unsigned.*"
				}
		);
	}

	@Test
	void SamlTest_OtherAudience_Scanner() {
		samlTest(
				new String[]{
						"OtherAudience"
				},
				new String[]{
						".*None of the audiences within Assertion .* matched the list of valid audiances.*"
				}
		);
	}


	@Test
	void SamlTest_OtherIssuer_Scanner() {
		samlTest(
				new String[]{
						"OtherIssuer"
				},
				new String[]{
						".*Invalid signature for object.*",
						".*Invalid issuer .* for SAML response.*",
						".*Issuer of Assertion .* did not match any valid issuers.*"
				}
		);
	}

	@Test
	void SamlTest_ReplayFirstPositiveSamlResponse_Scanner() {
		samlTest(
				new String[]{
						"ReplayFirstPositiveSamlResponse"
				},
				new String[]{
						".*The InResponseTo attribute .* does not match the ID of the authentication request.*",
						".*No subject confirmation methods were met for assertion.*",
						".*RelayState in response: .* differs from sent RelayState:.*"
				}
		);
	}

	@Test
	void SamlTest_OtherInResponseTo_Scanner() {
		samlTest(
				new String[]{
						"OtherInResponseTo"
				},
				new String[]{
						".*The InResponseTo attribute .* does not match the ID of the authentication request.*",
						".*No subject confirmation methods were met for assertion.*"
				}
		);
	}

	@Test
	void SamlTest_OtherRelayState_Scanner() {
		samlTest(
				new String[]{
						"OtherRelayState"
				},
				new String[]{
						".*RelayState in response: .* differs from sent RelayState:.*"
				}
		);
	}

	@Test
	void SamlTest_SuccessStatusIsFailed_Scanner() {
		samlTest(
				new String[]{
						"SuccessStatusIsFailed"
				},
				new String[]{
						".*Invalid status.*AuthnFailed.*"
				}
		);
	}
}
