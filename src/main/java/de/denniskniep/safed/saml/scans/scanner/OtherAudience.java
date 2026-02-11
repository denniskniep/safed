package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.data.SamlAuthData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OtherAudience extends SamlBaseScanner {

    @Override
    public SamlAuthData getAuthData(SamlAuthData samlAuthData) {
        samlAuthData.setAudiences(List.of("DoesNotExist"));
        return super.getAuthData(samlAuthData);
    }
}
