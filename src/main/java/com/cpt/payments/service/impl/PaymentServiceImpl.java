package com.cpt.payments.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cpt.payments.constant.Constant;
import com.cpt.payments.dto.CreateOrderReqDTO;
import com.cpt.payments.dto.OrderDTO;
import com.cpt.payments.http.HttpClientUtil;
import com.cpt.payments.http.HttpRequest;
import com.cpt.payments.paypal.res.Link;
import com.cpt.payments.paypal.res.OrderResponse;
import com.cpt.payments.service.helper.CreateOrderRequestHelper;
import com.cpt.payments.service.interfaces.PaymentService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private CreateOrderRequestHelper createOrderRequestHelper;

	private HttpClientUtil httpClientUtil;

	private Gson gson;

	public PaymentServiceImpl(Gson gson, HttpClientUtil httpClientUtil, 
			CreateOrderRequestHelper createOrderRequestProcessor) {
		this.gson = gson;
		this.httpClientUtil = httpClientUtil;
		this.createOrderRequestHelper = createOrderRequestProcessor;
	}

	@Override
	public OrderDTO createOrder(CreateOrderReqDTO createOrderReqDTO) {

		System.out.println("Creating order for "
				+ "createOrderReqDTO: " + createOrderReqDTO);


		HttpRequest httpRequest = createOrderRequestHelper.prepareRequest(
				createOrderReqDTO);

		System.out.println("Sending request to HttpClientUtil httpRequest: " + httpRequest);
		
		ResponseEntity<String> createOrderResponse = httpClientUtil.makeHttpRequest(httpRequest);
	
        String createOrderResponseAsJson = createOrderResponse.getBody();
		OrderResponse resAsObj = gson.fromJson(createOrderResponseAsJson, OrderResponse.class);

		System.out.println("Got createOrderResponse:** resAsObj" + resAsObj);

		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setId(resAsObj.getId());
		orderDTO.setStatus(resAsObj.getStatus());

		String redirectUrl = resAsObj.getLinks().stream()
				.filter(link -> Constant.REDIRECT_URL_PAYER_ACTION.equals(link.getRel()))
				.map(Link::getHref)
				.findFirst()
				.orElse(null);

		orderDTO.setRedirectUrl(redirectUrl);

		System.out.println("Returning created orderDTO: " + orderDTO);

		return orderDTO;

	}

}


