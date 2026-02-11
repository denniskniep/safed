package de.denniskniep.safed.common.auth;

import org.openqa.selenium.bidi.network.ResponseDetails;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationLog {

    private final List<RequestResponse> traffic = new ArrayList<>();

    public List<RequestResponse> getTraffic() {
        return traffic;
    }

    public RequestResponse add(String context, ResponseDetails responseDetails){
        RequestResponse requestResponse = new RequestResponse(context, responseDetails);
        traffic.add(requestResponse);
        return requestResponse;
    }
}


