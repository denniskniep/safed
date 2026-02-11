package de.denniskniep.safed;

import de.denniskniep.safed.common.config.IssuerConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

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
		exampleOidcApp.setIgnoredErrorDescriptions(new String[]{});
		exampleSamlApp.setIgnoredErrorDescriptions(new String[]{});
	}

	private void oidcTest(String[] triggeredScanners, String[] ignoredErrorDescriptions){
		exampleOidcApp.setIgnoredErrorDescriptions(ignoredErrorDescriptions);

		OidcClientConfig oidcClientConfig = oidcConfig.getClient(EXAMPLE_OIDC_CLIENT_ID).deepCopy();
		oidcClientConfig.setScanners(Arrays.asList(triggeredScanners));
		IssuerConfig issuerConfig = oidcConfig.getIssuer().deepCopy();
		var result = oidcAssessment.run(issuerConfig, oidcClientConfig);

		LOG.info(result.asJson());

		List<String> lastSeenErrorDescriptions = exampleOidcApp.getLastSeenErrorDescriptions();
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
	void OidcTest_NoVulnerabilities() {
		OidcClientConfig oidcClientConfig = oidcConfig.getClient(EXAMPLE_OIDC_CLIENT_ID).deepCopy();
		IssuerConfig issuerConfig = oidcConfig.getIssuer().deepCopy();
		var result = oidcAssessment.run(issuerConfig, oidcClientConfig);
		LOG.info(result.asJson());

		Assertions.assertEquals(ScanResultStatus.OK, result.getStatus());
		Assertions.assertEquals(0, result.getFindings().size());
	}

	@Test
	void OidcTest_BreakSignature_Scanner() {
		oidcTest(
				new String[]{
					"BreakSignatureOidcScanner"
				},
				new String[]{
					".*Invalid signature"
				}
			);
	}

	@Test
	void OidcTest_ExpiredBeforeIssue_Scanner() {
		oidcTest(
				new String[]{
					"ExpiredBeforeIssuedOidcScanner"
				},
				new String[]{
					".*expiresAt must be after issuedAt"
				}
		);
	}

	@Test
	void OidcTest_Expired_Scanner() {
		oidcTest(
				new String[]{
					"ExpiredOidcScanner"
				},
				new String[]{
						".*Jwt expired at .*",
						".*The ID Token contains invalid claims: .*exp=.*"
				}
		);
	}

	@Test
	void OidcTest_FutureNotBefore_Scanner() {
		oidcTest(
				new String[]{
						"FutureNotBeforeOidcScanner"
				},
				new String[]{
						".*Jwt used before .*"
				}
		);
	}

	@Test
	void OidcTest_NoneAlgAndNoSignature_Scanner() {
		oidcTest(
				new String[]{
					"NoneAlgOidcScanner",
				},
				new String[]{
					".*Invalid serialized unsecured/JWS/JWE object: Missing second delimiter",
				}
		);
	}

	@Test
	void OidcTest_NoSignatureOidcScanner_Scanner() {
		oidcTest(
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

		SamlClientConfig samlClientConfig = samlConfig.getClient(EXAMPLE_SAML_CLIENT_ID).deepCopy();
		samlClientConfig.setScanners(Arrays.asList(triggeredScanners));
		IssuerConfig issuerConfig = samlConfig.getIssuer().deepCopy();
		var result = samlAssessment.run(issuerConfig, samlClientConfig);

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
		SamlClientConfig samlClientConfig = samlConfig.getClient(EXAMPLE_SAML_CLIENT_ID).deepCopy();
		IssuerConfig samlIssuerConfig = samlConfig.getIssuer().deepCopy();
		var result = samlAssessment.run(samlIssuerConfig, samlClientConfig);
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
