package org.example;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Init ICICI PaymentGateway!" );

        PaymentGateway gateway = new PaymentGateway();
        try {
            // Generate txnDate in YYYYMMDDHHMMSS format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Long txnDate = Long.parseLong(sdf.format(new Date()));
            String merchantTxnNo = "A" + txnDate; // Unique merchant transaction number

            String paymentUrl = gateway.initiatePayment(
                    100000000008164L,
                "A100000000008164",
                merchantTxnNo,
                100.00,
                356,
                0,
                "Dummyemail@icicibank.com",
                "SALE",
                "https://pgpayuat.icicibank.com/tsp/pg/api/merchant",
                txnDate,
                "9999999999",
                "Ashok Parmar",
                "TestBilling",
                "0012345"
            );
            System.out.println("Payment URL: " + paymentUrl);
        } catch (Exception e) {
            System.err.println("Error initiating payment: " + e.getMessage());
        }
    }
}
