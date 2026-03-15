package de.denniskniep.safed.common.auth.browser;

import de.denniskniep.safed.common.auth.browser.bidi.ResponseDataDetails;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class AuthenticationLog {

    private final List<RequestResponse> traffic = new ArrayList<>();

    public List<RequestResponse> getTraffic() {
        return traffic;
    }

    public RequestResponse add(ResponseDataDetails responseDataDetails){
       return add("", responseDataDetails);
    }

    public void addAll(String context, List<RequestResponse> requestResponses){
        for (RequestResponse r : requestResponses) {
            RequestResponse requestResponse = new RequestResponse(r.created(), context, r.responseDataDetails());
            traffic.add(requestResponse);
        }
    }

    public RequestResponse add(String context, ResponseDataDetails responseDataDetails){
        RequestResponse requestResponse = new RequestResponse(context, responseDataDetails);
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

    public Optional<RequestResponse> find(Predicate<RequestResponse> findCondition){
        return findInternal(Optional.empty(), findCondition);
    }

    public Optional<RequestResponse> findStartingAt(String startAtRequestId, Predicate<RequestResponse> findCondition){
        return findInternal(Optional.of(startAtRequestId), findCondition);
    }

    private Optional<RequestResponse> findInternal(Optional<String> startAtRequestId, Predicate<RequestResponse> findCondition){
        var execFind = false;
        for (RequestResponse t : List.copyOf(traffic)) {
            if (startAtRequestId.isEmpty() || StringUtils.equals(t.getRequest().getRequestId(), startAtRequestId.get())){
                execFind = true;
            }

            if(execFind && findCondition.test(t)){
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    public String asShortLogList(){
        return String.join("\n",getTraffic().stream().map(RequestResponse::asShortLog).toList());
    }
}


