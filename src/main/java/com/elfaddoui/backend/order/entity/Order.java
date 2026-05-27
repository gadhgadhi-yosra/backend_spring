package com.elfaddoui.backend.order.entity;

import com.elfaddoui.backend.common.entity.AuditableEntity;
import com.elfaddoui.backend.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 32, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliverySlot deliverySlot = DeliverySlot.ASAP;

    @Column(length = 80)
    private String scheduledTime;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column
    private String email;

    @Column(length = 2000)
    private String note;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 120)
    private String area;

    @Column(nullable = false)
    private String street;

    @Column
    private String extra;

    @Column(length = 40)
    private String postalCode;

    @Column
    private String addressHint;

    @Column(length = 80)
    private String placeType;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 120)
    private String courierName;

    @Column(length = 40)
    private String courierPhone;

    @Column(length = 40)
    private String storePhone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getReference() {
        return reference;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public DeliverySlot getDeliverySlot() {
        return deliverySlot;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getNote() {
        return note;
    }

    public String getCity() {
        return city;
    }

    public String getArea() {
        return area;
    }

    public String getStreet() {
        return street;
    }

    public String getExtra() {
        return extra;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getAddressHint() {
        return addressHint;
    }

    public String getPlaceType() {
        return placeType;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getCourierName() {
        return courierName;
    }

    public String getCourierPhone() {
        return courierPhone;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setDeliverySlot(DeliverySlot deliverySlot) {
        this.deliverySlot = deliverySlot;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setAddressHint(String addressHint) {
        this.addressHint = addressHint;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public void setCourierPhone(String courierPhone) {
        this.courierPhone = courierPhone;
    }

    public void setStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }
}
