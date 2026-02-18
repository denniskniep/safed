package de.denniskniep.safed.common.auth.browser;

import org.apache.commons.lang3.StringUtils;
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

    public void clearTrafficAfter(String requestId) {
        var removeItem = false;

        for (RequestResponse requestResponse : traffic.stream().toList()) {
            if(removeItem){
                traffic.remove(requestResponse);
            }

            if(StringUtils.equals(requestResponse.getRequest().getRequestId(), requestId)){
                removeItem =  true;
            }
        }
    }
}


