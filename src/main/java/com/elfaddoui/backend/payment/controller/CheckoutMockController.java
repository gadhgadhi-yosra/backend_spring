package com.elfaddoui.backend.payment.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dev-friendly mock checkout page.
 *
 * The mobile app opens the checkoutUrl in an external browser (Safari/Chrome). In local dev,
 * pointing the checkout base URL to {@code http://127.0.0.1:8080/pay} avoids "domain not found"
 * issues and provides a visible page for debugging.
 */
@RestController
public class CheckoutMockController {

    @GetMapping(value = "/pay", produces = MediaType.TEXT_HTML_VALUE)
    public String pay(
            @RequestParam(required = false) String paymentIntentId,
            @RequestParam(required = false) String amount,
            @RequestParam(required = false) String currency
    ) {
        String safePi = escape(paymentIntentId);
        String safeAmount = escape(amount);
        String safeCurrency = escape(currency);

        // Use plain replacements (not String.format) so literal '%' in CSS is safe.
        return """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1"/>
                    <title>Checkout (dev)</title>
                    <style>
                      body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif; margin: 0; background: #0b1020; color: #e6e8f0; }
                      .wrap { max-width: 720px; margin: 40px auto; padding: 0 16px; }
                      .card { background: #111833; border: 1px solid #24305a; border-radius: 10px; padding: 18px; }
                      h1 { font-size: 18px; margin: 0 0 12px; }
                      .muted { color: #a9b0c7; font-size: 13px; line-height: 1.4; }
                      table { width: 100%; border-collapse: collapse; margin-top: 12px; }
                      td { padding: 8px 0; border-bottom: 1px solid #24305a; font-size: 14px; }
                      td.key { color: #a9b0c7; width: 44%; }
                      code { background: #0b1020; padding: 2px 6px; border-radius: 6px; border: 1px solid #24305a; }
                      .actions { display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap; }
                      a.btn { display: inline-block; padding: 10px 12px; border-radius: 8px; text-decoration: none; border: 1px solid #2e3c72; color: #e6e8f0; background: #18224a; font-size: 14px; }
                    </style>
                  </head>
                  <body>
                    <div class="wrap">
                      <div class="card">
                        <h1>Checkout (dev mock)</h1>
                        <div class="muted">
                          This page is meant for local development only. In production, configure <code>app.payment.checkout-base-url</code>
                          to a publicly reachable checkout domain (for example <code>https://checkout.elfaddoui.tn/pay</code>).
                        </div>
                        <table>
                          <tr><td class="key">paymentIntentId</td><td><code>{{paymentIntentId}}</code></td></tr>
                          <tr><td class="key">amount</td><td><code>{{amount}}</code></td></tr>
                          <tr><td class="key">currency</td><td><code>{{currency}}</code></td></tr>
                        </table>
                        <div class="actions">
                          <a class="btn" href="javascript:history.back()">Back</a>
                          <a class="btn" href="/">API Home</a>
                        </div>
                      </div>
                    </div>
                  </body>
                </html>
                """
                .replace("{{paymentIntentId}}", safePi)
                .replace("{{amount}}", safeAmount)
                .replace("{{currency}}", safeCurrency);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
