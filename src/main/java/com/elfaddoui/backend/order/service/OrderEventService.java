package com.elfaddoui.backend.order.service;

import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.order.dto.DeliveryTrackingResponse;
import com.elfaddoui.backend.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderEventService {

    private final OrderRepository orderRepository;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> listeners = new ConcurrentHashMap<>();

    public OrderEventService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public SseEmitter subscribe(String userEmail, String orderReference, DeliveryTrackingResponse initialPayload) {
        orderRepository.findByReferenceIgnoreCaseAndUserEmail(orderReference, userEmail)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        String refKey = normalizeRef(orderReference);
        SseEmitter emitter = new SseEmitter(0L);
        listeners.computeIfAbsent(refKey, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(refKey, emitter));
        emitter.onTimeout(() -> remove(refKey, emitter));
        emitter.onError(error -> remove(refKey, emitter));

        try {
            emitter.send(SseEmitter.event().name("tracking").data(initialPayload));
        } catch (Exception exception) {
            remove(refKey, emitter);
            throw new IllegalStateException("Unable to start order event stream", exception);
        }

        return emitter;
    }

    public void publish(String orderReference, DeliveryTrackingResponse payload) {
        String refKey = normalizeRef(orderReference);
        List<SseEmitter> emitters = listeners.getOrDefault(refKey, new CopyOnWriteArrayList<>());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("tracking").data(payload));
            } catch (Exception exception) {
                remove(refKey, emitter);
            }
        }
    }

    private void remove(String orderReference, SseEmitter emitter) {
        List<SseEmitter> emitters = listeners.get(orderReference);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            listeners.remove(orderReference, emitters);
        }
    }

    private String normalizeRef(String orderReference) {
        return orderReference == null ? "" : orderReference.trim().toUpperCase();
    }
}
