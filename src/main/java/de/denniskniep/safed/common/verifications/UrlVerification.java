package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UrlVerification extends PartsVerification {

    @Override
    protected String getEvidenceType() {
        return "Url";
    }

    @Override
    protected String asString(Map<String, String> parts){
        var url = new StringBuilder();

        if(parts.containsKey("Scheme")){
            url.append(parts.get("Scheme")).append("://");
        }
        if(parts.containsKey("Host")){
            url.append(parts.get("Host"));
        }
        if(parts.containsKey("Port")){
            url.append(":").append(parts.get("Port"));
        }

        url.append(pathAsString(parts));

        var query = queryAsString(parts);
        if(!query.isEmpty()){
            url.append("?").append(query);
        }

        if(parts.containsKey("Fragment")){
            url.append("#").append(parts.get("Fragment"));
        }

        return url.toString();
    }

    private String pathAsString(Map<String, String> parts){
        var segments = parts.keySet().stream()
                .filter(k -> k.startsWith("Path."))
                .sorted(Comparator.comparingInt(k -> Integer.parseInt(k.substring("Path.".length()))))
                .map(parts::get)
                .toList();

        if(segments.isEmpty()){
            return "";
        }
        return "/" + String.join("/", segments);
    }

    private String queryAsString(Map<String, String> parts){
        return parts.keySet().stream()
                .filter(k -> k.startsWith("Query."))
                .sorted()
                .map(k -> k.substring("Query.".length()) + "=" + parts.get(k))
                .collect(Collectors.joining("&"));
    }

    @Override
    protected Map<String, String> extractParts(AuthResult authResult){
        var parts = new HashMap<String, String>();
        if(authResult.getResponsePage() == null){
            return parts;
        }

        var url = authResult.getResponsePage().url();
        if(StringUtils.isBlank(url)){
            return parts;
        }

        UriComponents components;
        try {
            components = UriComponentsBuilder.fromUriString(url).build();
        } catch (IllegalArgumentException e) {
            return parts;
        }

        putIfPresent(parts, "Scheme", components.getScheme());
        putIfPresent(parts, "Host", components.getHost());
        if(components.getPort() != -1){
            parts.put("Port", String.valueOf(components.getPort()));
        }
        putPathSegments(parts, components.getPath());
        putIfPresent(parts, "Fragment", components.getFragment());
        putQueryParams(parts, components);

        return parts;
    }

    private void putIfPresent(Map<String, String> parts, String name, String value){
        if(value != null){
            parts.put(name, value);
        }
    }

    private void putPathSegments(Map<String, String> parts, String path){
        if(StringUtils.isBlank(path)){
            return;
        }

        int index = 0;
        for (String segment : path.split("/")){
            if(segment.isEmpty()){
                continue;
            }
            parts.put("Path." + index, segment);
            index++;
        }
    }

    private void putQueryParams(Map<String, String> parts, UriComponents components){
        components.getQueryParams().forEach((name, values) ->
            parts.put("Query." + name, String.join(",", values))
        );
    }
}
