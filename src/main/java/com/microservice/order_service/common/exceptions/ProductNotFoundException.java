package com.microservice.order_service.common.exceptions;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(String msg){super(msg);}
}
