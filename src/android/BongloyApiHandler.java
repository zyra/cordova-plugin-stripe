package com.stripe.android;

import android.support.annotation.NonNull;

import com.stripe.android.exception.APIConnectionException;
import com.stripe.android.exception.InvalidRequestException;
import com.stripe.android.model.Token;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class BongloyApiHandler extends StripeApiHandler {

    private static final String LIVE_API_BASE = "https://api.bongloy.com";
    private static final String TOKENS = "tokens";
    private static final String CHARSET = "UTF-8";

    static String getApiUrl() {
        return String.format(Locale.ENGLISH, "%s/v1/%s", LIVE_API_BASE, TOKENS);
    }

    static Token createToken(
            @NonNull Map<String, Object> tokenParams,
            @NonNull RequestOptions options)
            throws InvalidRequestException, APIConnectionException {

        StripeResponse response = getStripeResponse(getApiUrl(), tokenParams, options);
        return Token.fromString(response.getResponseBody());
    }

    private static byte[] getOutputBytes(@NonNull Map<String, Object> params) throws InvalidRequestException {
        try {
            String query = createQuery(params);
            return query.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new InvalidRequestException("Unable to encode parameters to "
                    + CHARSET
                    + ". Please contact support@stripe.com for assistance.",
                    null, null, 0, e);
        }
    }

    private static java.net.HttpURLConnection createPostConnection(
            @NonNull String url,
            @NonNull Map<String, Object> params,
            @NonNull RequestOptions options) throws IOException, InvalidRequestException {
        java.net.HttpURLConnection conn = createStripeConnection(url, options);

        conn.setDoOutput(true) ;
        conn.setRequestMethod(POST);
        conn.setRequestProperty("Content-Type", String.format("application/x-www-form-urlencoded;charset=%s", CHARSET));

        OutputStream output = null;
        try {
            output = conn.getOutputStream();
            output.write(getOutputBytes(params));
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return conn;
    }

    private static boolean urlNeedsHeaderData(@NonNull String url) {
        return url.startsWith(LIVE_API_BASE);
    }

    private static java.net.HttpURLConnection createStripeConnection(
            String url,
            RequestOptions options)
            throws IOException {
        URL stripeURL;

        stripeURL = new URL(url);
        HttpURLConnection conn  = (HttpURLConnection) stripeURL.openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(80 * 1000);
        conn.setUseCaches(false);

        if (urlNeedsHeaderData(url)) {
            for (Map.Entry<String, String> header : getHeaders(options).entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        return conn;
    }

    private static String getResponseBody(InputStream responseStream)
            throws IOException {
        Scanner scanner = new Scanner(responseStream, CHARSET).useDelimiter("\\A");
        String rBody = scanner.hasNext() ? scanner.next() : null;
        responseStream.close();
        return rBody;
    }

    private static StripeResponse getStripeResponse(
            String url,
            Map<String, Object> params,
            RequestOptions options)
            throws InvalidRequestException, APIConnectionException {
            java.net.HttpURLConnection conn = null;
        try {
            conn = createPostConnection(url, params, options);

            // trigger the request
            int rCode = conn.getResponseCode();
            String rBody;
            Map<String, List<String>> headers;

            if (rCode >= 200 && rCode < 300) {
                rBody = getResponseBody(conn.getInputStream());
            } else {
                rBody = getResponseBody(conn.getErrorStream());
            }
            headers = conn.getHeaderFields();
            return new StripeResponse(rCode, rBody, headers);

        } catch (IOException e) {
            throw new APIConnectionException(
                    String.format(
                            "IOException during API request to Bongloy (%s): %s "
                                    + "Please check your internet connection and try again. "
                                    + "or let us know at support@bongloy.com.",
                            getApiUrl(), e.getMessage()), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
