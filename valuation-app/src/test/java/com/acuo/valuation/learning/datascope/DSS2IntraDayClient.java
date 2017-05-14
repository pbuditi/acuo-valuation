package com.acuo.valuation.learning.datascope;/*
 * ====================================================================
 * This Java example demonstrate how to make an HTTPS request to DSS2
 * for an Intraday On Demand Extraction report.
 * 
 * To run this example, you must provide a DSS2 username and password.
 * ====================================================================
 */

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONOrderedObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Scanner;

public class DSS2IntraDayClient {
    private String urlHost = "https://hosted.datascopeapi.reuters.com/RestApi/v1";
    private String sessionToken = "";
    private CloseableHttpClient httpclient = HttpClientBuilder.create().build();

    public static void main(String[] args) throws Exception {

        if(args.length < 2) {
            System.out.println("Please enter DSS2 Username as 1st command line parameter, Password as 2nd");
            System.exit(-1);
        }

        DSS2IntraDayClient dss2 = new DSS2IntraDayClient();
        dss2.getSessionToken(args[0], args[1]);
        promptEnterKey();

        // Intraday On Demand Extraction report
        dss2.intraDayExtractRequest();

        dss2.httpclient.close();
    }


    /**
     * Request DSS2 for a Session Token (24 hour life)
     * 
     * @param username
     * @param password
     */
    public void getSessionToken(String username, String password) {

        try {
            HttpPost httppost = new HttpPost(urlHost + "/Authentication/RequestToken");

            httppost.addHeader("content-type", "application/json; charset=UTF-8");

            JSONObject TokenRequest = new JSONObject()
                .put("Credentials", new JSONObject()
                    .put("Username",  username)
                    .put("Password",  password));

            StringEntity requestBody = new StringEntity(TokenRequest.toString());

            httppost.setEntity(requestBody);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            String response = httpclient.execute(httppost, responseHandler);
            JSONObject jsonResponse = new JSONObject(response);

            sessionToken = jsonResponse.get("value").toString();
            System.out.println("Session Token (expires in 24 hours):\n" + sessionToken);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This is an Intraday On Demand Extraction Request
     * 
     * Here is the reference doc for the Async mechanism:
     * https://hosted.datascopeapi.reuters.com/RestApi.Help/Home/KeyMechanisms#sectionAsync
     * 
     */
    public void intraDayExtractRequest() {

        try {
            HttpPost httppost = new HttpPost(urlHost + "/Extractions/ExtractWithNotes");

            httppost.addHeader("content-type", "application/json;odata.metadata=minimal");
            httppost.addHeader("Authorization", "Token " + sessionToken);
            // Use the 1st line for your program, the 2nd line to test a timeout (HTTP status 202):
            httppost.addHeader("Prefer", "respond-async");
            //httppost.addHeader("Prefer", "respond-async, wait=5");

            JSONOrderedObject intradayExtractionJSONObject = new JSONOrderedObject()
                .put("ExtractionRequest", new JSONOrderedObject()
                    .put("@odata.type", "#ThomsonReuters.Dss.Api.Extractions.ExtractionRequests.IntradayPricingExtractionRequest")
                    .put("ContentFieldNames", new JSONArray()
                        .put("RIC")
                        .put("Mid Price")
                        .put("Last Update Time"))
                    .put("IdentifierList", new JSONOrderedObject()
                        .put("@odata.type", "#ThomsonReuters.Dss.Api.Extractions.ExtractionRequests.InstrumentIdentifierList")
                        .put("InstrumentIdentifiers", new JSONArray()
                            .put(new JSONObject()
                                .put("Identifier", "USDEUR=R")
                                .put("IdentifierType", "Ric")))
                        .put("ValidationOptions", JSONObject.NULL)
                        .put("UseUserPreferencesForValidationOptions", false))
                    .put("Condition", new JSONOrderedObject()
                        .put( "ScalableCurrency", true)));

            System.out.println("Intraday extraction JSON request content:\n"+ intradayExtractionJSONObject.toString() +"\n");
            StringEntity requestBody = new StringEntity(intradayExtractionJSONObject.toString());

            httppost.setEntity(requestBody);

            // NOTE: If the extraction request takes more than 30 seconds the async mechanism will be used. 
            HttpResponse response = httpclient.execute(httppost);

            StringBuffer result = new StringBuffer();

            // Get the response status code
            int respStatusCode = response.getStatusLine().getStatusCode();

            switch (respStatusCode) {
            case 200:    // if the status code is 200, then a response is available now
                System.out.println("HTTP status: " + respStatusCode + " - A response is available now!" );
                result = getResult(response);
                treatResult(result);
                break;

            case 202:    // If the request takes longer than the poll time, a status code 202 (accepted)
                         // is returned to the client, with a location URL to poll for the status.    
                         // The request is accepted, but the result is not yet ready.
                System.out.println("HTTP status: " + respStatusCode + " - We must wait, and poll the location URL." );

                Header[] headers = response.getAllHeaders();
                String pollURL = "";
                for (int i= 0; i< headers.length; i++)
                {
                    if (headers[i].getName().equalsIgnoreCase("Location")) {
                        pollURL = headers[i].getValue();
                        System.out.println("The location URL: " + pollURL);
                    }
                }

                wait(30);

                HttpGet requestGet = new HttpGet(pollURL);
                requestGet.addHeader("Authorization", "Token "+sessionToken);
                requestGet.addHeader("Prefer", "respond-async");

                HttpResponse responseGet = httpclient.execute(requestGet);
                respStatusCode = responseGet.getStatusLine().getStatusCode();
                System.out.println("\nHTTP status: " + respStatusCode);

                // Poll the location URL until the extraction is completed:
                while (respStatusCode == 202) {
                    wait(30);
                    responseGet = httpclient.execute(requestGet);
                    respStatusCode = responseGet.getStatusLine().getStatusCode();
                    System.out.println("\nHTTP status: " + respStatusCode);
                }

                // A response should be available now:
                if (respStatusCode == 200) {
                    result = getResult(responseGet);
                	treatResult(result);
                }
                break;

            case 400:
                System.out.println("ERROR: HTTP status: 400 (Bad Request).  Request content is malformed and cannot be parsed.");
                break;
            case 401:
                System.out.println("ERROR: HTTP status: 401 (Unauthorized).  Authentication token is invalid or has expired.");
                break;
            case 403:
                System.out.println("ERROR: HTTP status: 403 (Forbidden).  Account not permissioned for this type of data.");
                break;
            default:
                System.out.println("ERROR: Cannot proceed. Please check the meaning of HTTP status " + respStatusCode);
                break;
        }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public StringBuffer getResult(HttpResponse response) {

    	StringBuffer result = new StringBuffer();

    	try {
            BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void treatResult(StringBuffer result) {

        System.out.println("Result received:\n"+result);

        // Convert the response string to a JSON object, to parse its contents:
        JSONObject jsonResponse = new JSONObject(result.toString());

        StringWriter out = new StringWriter();
        jsonResponse.write(out);
        System.out.println("JSON response:\n"+ out +"\n");

        // Pull out the extraction notes, check for success:
        JSONArray notes = jsonResponse.getJSONArray("Notes");
        out = new StringWriter();
        notes.write(out);
        System.out.println("Notes extracted from JSON response:\n"+ out);
        if (out.toString().contains("Processing completed successfully")) {
            System.out.println("SUCCESS: Processing completed successfully !\n");
        } else {
    	    System.out.println("ERROR: Processing did not complete successfully !\n");
        }

        // Pull out the data content:
        JSONArray jarray = jsonResponse.getJSONArray("Contents");
        out = new StringWriter();
        jarray.write(out);
        System.out.println("Content extracted from JSON response:\n"+ out);
    }


    public static void wait(int seconds) {
        try {
            System.out.println("Waiting " + seconds + " seconds ..");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }


    public static void promptEnterKey() {
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
} //EOF