package papyal_demo.Payemt_gateway.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import papyal_demo.Payemt_gateway.service.PaypalService;
@org.springframework.stereotype.Controller
@RequiredArgsConstructor
@Slf4j
public class Controller {
    	
    //private static final Logger logger = LoggerFactory.getLogger(Controller.class);
@Autowired
	private PaypalService paypalService;
	@GetMapping("/")
	public String home() {
		return "index";
	}
	@PostMapping("/payment/create")
	public RedirectView createPayment(@RequestParam ("method") String method,
			@RequestParam ("amount") String amount,
			@RequestParam ("currency") String currency,
			@RequestParam ("description") String description
			) {
	    try {
	        String cancelUrl = "http://localhost:8080/payment/cancel";
	        String successUrl = "http://localhost:8080/payment/success";

	        Payment payment = paypalService.createPayment(
	           Double.valueOf(amount),
	            currency,
	            method, // payment method
	            "sale", // payment intent
	            description, // description
	            cancelUrl, // cancel URL
	            successUrl // success URL
	        );

	        // Loop through the links provided by PayPal to find the approval link
	        for (Links link : payment.getLinks()) {
	            if (link.getRel().equals("approval_url")) {
	                return new RedirectView(link.getHref()); // Redirect to PayPal approval
	            }
	        }
	    } catch (PayPalRESTException e) {
	        // Log the error and return the error view
	        System.out.println("Error occurred during payment creation: " + e.getMessage());
	    }

	    // If payment creation fails, redirect to an error page
	    return new RedirectView("/payment/error");
	}

	@GetMapping("/payment/success")
	public String paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
		try {
		Payment payment = paypalService.PaymentExecute(paymentId, payerId);
				
		if (payment.getState().equals("approved")) {
			return "paymentSuccess";
		}
		}
		catch(PayPalRESTException e) {
			//logger.error("Error occurred::",e);
			System.out.println("Error : "+" "+e);
		}
		return "paymentSuccess";
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
