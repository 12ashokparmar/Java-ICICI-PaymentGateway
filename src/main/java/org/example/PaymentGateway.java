package org.example;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;

import java.security.cert.X509Certificate;

public class PaymentGateway {

    private static final String API_URL = "https://pgpayuat.icicibank.com/tsp/pg/api/v2/initiateSale"; // Placeholder URL
    private static final String API_KEY = ""; // Replace with actual API key
    //private static final String SECRET_KEY = ""; // Replace with actual secret key

    public String initiatePayment(Long merchantId, String aggregatorID, String merchantTxnNo, Double amount, Integer currencyCode, Integer payType, String customerEmailID, String transactionType, String returnURL, Long txnDate, String customerMobileNo, String customerName, String addlParam1, String addlParam2) throws IOException {
        String secureHash = generateSecureHash(addlParam1, addlParam2, aggregatorID, amount, currencyCode, customerEmailID, customerMobileNo, customerName, merchantId, merchantTxnNo, payType, returnURL, transactionType, txnDate);

       System.out.println("Generated Secure Hash: " + secureHash);

        CloseableHttpClient httpClient = createHttpClient();
        HttpPost httpPost = new HttpPost(API_URL);

        // Set headers
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + API_KEY);

        // Create JSON payload
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new PaymentRequest(merchantId, aggregatorID, merchantTxnNo, amount, currencyCode, payType, customerEmailID, transactionType, returnURL, txnDate, customerMobileNo, customerName, addlParam1, addlParam2, secureHash));

        System.out.println("Request JSON: " + json);

        httpPost.setEntity(new StringEntity(json));

        // Execute request
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            
            System.out.println("Response Status Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);
            
            // Check if response is HTML (error page) instead of JSON
            if (responseBody.trim().startsWith("<")) {
                throw new IOException("Server returned HTML error page. Status Code: " + statusCode + ". Response: " + responseBody);
            }
            
            JsonNode responseJson = mapper.readTree(responseBody);

            System.out.println("Parsed JSON Response: " +responseBody);

            System.out.println("Parsed JSON Response: " +"https://pgpayuat.icicibank.com/tsp/pg/api/v2/authRedirect?tranCtx="+responseJson.get("tranCtx").asText());

            // Assuming response has paymentUrl or redirectUrl
            if (responseJson.has("paymentUrl")) {
                return responseJson.get("paymentUrl").asText();
            } else if (responseJson.has("redirectURI")) {
                return responseJson.get("redirectURI").asText();
            } else {
                throw new IOException("Payment initiation failed: " + responseBody);
            }
        }
    }

    private CloseableHttpClient createHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return HttpClients.custom().setSSLContext(sc).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /*
        addlParam1
        addlParam2
        aggregatorID
        amount
        currencyCode
        customerEmailID
        customerMobileNo
        customerName
        merchantId
        merchantTxnNo
        payType
        returnURL
        transactionType
        txnDate

    */
    private String generateSecureHash(String addlParam1, String addlParam2, String aggregatorID, Double amount, Integer currencyCode, String customerEmailID, String customerMobileNo, String customerName, Long merchantId, String merchantTxnNo, Integer payType, String returnURL, String transactionType, Long txnDate) {

        String msg = addlParam1 + addlParam2 + aggregatorID + amount +
                currencyCode + customerEmailID + customerMobileNo + customerName + merchantId + merchantTxnNo + payType + returnURL + transactionType + txnDate;

        System.out.println("String to be hashed: " + msg);

        //msg = "ABCD111A100000000007164100.00356narayan.kapase@phicommerce.com917709356362Narayan1000000000071647575858875750https://pgpayuat.icicibank.com/tsp/pg/api/merchantSALE20241121115413";
        String digest = null;
        try {
            SecretKeySpec key = new SecretKeySpec((API_KEY).getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));
            StringBuffer hash = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            digest = hash.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return digest;
    }

    private static class PaymentRequest {
        public Long merchantId;
        public String aggregatorID;
        public String merchantTxnNo;
        public Double amount;
        public Integer currencyCode;
        public Integer payType;
        public String customerEmailID;
        public String transactionType;
        public String returnURL;
        public Long txnDate;
        public String customerMobileNo;
        public String customerName;
        public String addlParam1;
        public String addlParam2;
        public String secureHash;

        public PaymentRequest(Long merchantId, String aggregatorID, String merchantTxnNo, Double amount, Integer currencyCode, Integer payType, String customerEmailID, String transactionType, String returnURL, Long txnDate, String customerMobileNo, String customerName, String addlParam1, String addlParam2, String secureHash) {
            this.merchantId = merchantId;
            this.aggregatorID = aggregatorID;
            this.merchantTxnNo = merchantTxnNo;
            this.amount = amount;
            this.currencyCode = currencyCode;
            this.payType = payType;
            this.customerEmailID = customerEmailID;
            this.transactionType = transactionType;
            this.returnURL = returnURL;
            this.txnDate = txnDate;
            this.customerMobileNo = customerMobileNo;
            this.customerName = customerName;
            this.addlParam1 = addlParam1;
            this.addlParam2 = addlParam2;
            this.secureHash = secureHash;
        }
    }
}
