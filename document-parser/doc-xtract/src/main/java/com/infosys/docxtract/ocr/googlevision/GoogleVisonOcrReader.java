package com.infosys.docxtract.ocr.googlevision;

import com.infosys.docxtract.ocr.*;
import org.springframework.web.client.*;
import org.slf4j.*;
import org.springframework.util.*;
import org.springframework.http.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import com.infosys.docxtract.ocr.googlevision.model.*;
import java.util.*;

public class GoogleVisonOcrReader extends AbstarctOcrReader<String>
{
    Logger logger;
    Request request;
    private String apiKey;
    private String url;
    private RestTemplate restTemplate;
    
    public GoogleVisonOcrReader(final String url, final String apiKey) {
        this.logger = LoggerFactory.getLogger((Class) GoogleVisonOcrReader.class);
        this.url = url;
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public String readTextFromImage(final String base64Image,String featureType) {

        this.request = buildRequest(featureType);
        final Image image = new Image();
        image.setContent(base64Image);
        final List<OCRRequest> list = (List<OCRRequest>)this.request.getRequests();
        final OCRRequest ocrRequest = list.get(0);
        ocrRequest.setImage(image);
        list.clear();
        list.add(ocrRequest);
        this.request.setRequests((List)list);
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept((List)Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<Request> entity = (HttpEntity<Request>)new HttpEntity((Object)this.request, (MultiValueMap)headers);
        final String resp = (String)this.restTemplate.exchange(this.url + this.apiKey, HttpMethod.POST, (HttpEntity)entity, (Class)String.class, new Object[0]).getBody();
        this.logger.info("Google vision Response: "+ resp);
        try {
            final JSONObject jsonObject = (JSONObject)new JSONParser().parse(resp);
            final JSONArray jsonArray = (JSONArray)jsonObject.get((Object)"responses");
            final JSONObject object = (JSONObject)jsonArray.get(0);
            final JSONArray jsonArray2 = (JSONArray)object.get((Object)"textAnnotations");
            final JSONObject object2 = (JSONObject)jsonArray2.get(0);
            final String str = String.valueOf(object2.get((Object)"description"));
            this.logger.info("Google vision Result: " + str);
            return str;
        }
        catch (ParseException e) {
            this.logger.error("Cannot parse response json object: " + e.getMessage());
            return "";
        }
        catch (NullPointerException e2) {
            this.logger.error("Null pointer exception: " + e2.getMessage());
            return "";
        }
    }

    //build request
    private Request buildRequest(String featureType) {
        final Feature feature = new Feature();
        feature.setType(featureType);
        final ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        final OCRRequest request = new OCRRequest();
        request.setFeatures((List)features);
        final ArrayList<OCRRequest> requests = new ArrayList<OCRRequest>();
        requests.add(request);
        final Request req = new Request();
        req.setRequests((List)requests);
        return req;
    }
}