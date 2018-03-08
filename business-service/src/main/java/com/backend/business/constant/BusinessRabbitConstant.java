package com.backend.business.constant;


public class BusinessRabbitConstant {

    public static class Exchange {
        public static final String SEND_CASH_PRIZE_EXCHANGE = "business-send-cash-prize-exchange";
    }

    public static class Queue {
        public static final String SEND_CASH_PRIZE_QUEUE = "business-send-cash-prize-queue";
    }

    public static class Key {
        public static final String SEND_CASH_PRIZE_ROUTINGKEY = "SEND_CASH_PRIZE_KEY";
    }

}
