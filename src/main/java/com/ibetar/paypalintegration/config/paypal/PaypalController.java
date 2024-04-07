package com.ibetar.paypalintegration.config.paypal;

import com.ibetar.paypalintegration.config.HttpUtilHelpers;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaypalController {
    private final PaypalService paypalService;
    @Value("${paypal-redirects.urls.cancelUrl}")
    private String cancelUrl;
    @Value("${paypal-redirects.urls.successUrl}")
    private String successUrl;

    private final HttpUtilHelpers httpUtil;
    @GetMapping("/")
    public String home() { return "index"; }

    @PostMapping("/payment/create")
    public RedirectView createPayment(
            @RequestParam("amount") Double amount,
            @RequestParam("currency") String currency,
            @RequestParam("method") String method,
            @RequestParam("description") String description
    ) {
        try {

            Payment payment = paypalService.createPayment(
                    amount,
                    currency,
                    method,
                    "sale",
                    description,
                    cancelUrl,
                    successUrl
            );

            for (Links links: payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return new RedirectView(links.getHref());
                }
            }

        } catch (PayPalRESTException e) {
            log.error("Error occurred while creating payment: {}", e.getMessage());
        }
        return new RedirectView("/payment/error");
    }

    @GetMapping("/payment/success")
    public ModelAndView confirmPaymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId
    ) {
        log.info("executing Payment");

        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                return new ModelAndView(
                        "redirect:" + httpUtil.getAppUrl() +
                                "/payment-success?state=" + payment.getState()
                );
            }

        } catch (PayPalRESTException e) {
            log.error("Error while executing Payment: {}", e.getMessage());
            String redirectUrlError = httpUtil.getAppUrl() + "/error?error=" + e.getMessage();
            return new ModelAndView("redirect:" + redirectUrlError);
        }

        String redirectUrlError = httpUtil.getAppUrl() +
                "/error?error=" + "Unknown error while executing Payment";
        return new ModelAndView("redirect:" + redirectUrlError);
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "paymentCancel";
    }

    @GetMapping("/payment/error")
    public String paymentError() {
        return "paymentError";
    }
}