package com.shopapp.order;

import com.shopapp.address.Address;
import com.shopapp.category.Category;
import com.shopapp.category.CategoryRepository;
import com.shopapp.discount.Discount;
import com.shopapp.discount.DiscountRepository;
import com.shopapp.orderproduct.OrderProduct;
import com.shopapp.orderproduct.OrderProductRepository;
import com.shopapp.product.Product;
import com.shopapp.product.ProductRepository;
import com.shopapp.user.Gender;
import com.shopapp.user.Role;
import com.shopapp.user.User;
import com.shopapp.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private OrderRepository orderRepository;
    private List<Order> testOrders = new ArrayList<>();
    private User user1;
    private User user2;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder()
                .firstName("Steven")
                .lastName("Smith")
                .email("mail@mail.com")
                .password("password")
                .birthDate(LocalDateTime.now())
                .gender(Gender.MALE)
                .phoneNumber("111222333")
                .role(Role.ROLE_ADMIN)
                .address(Address.builder()
                        .country("Germany")
                        .region("Bavaria")
                        .city("Munich")
                        .street("Shlasse")
                        .number("23")
                        .postalCode("21-444")
                        .build())
                .build());

        user2 = userRepository.save(User.builder()
                .firstName("Albert")
                .lastName("Smith")
                .email("mail1@mail.com")
                .password("password")
                .birthDate(LocalDateTime.now())
                .gender(Gender.MALE)
                .phoneNumber("111222444")
                .role(Role.ROLE_ADMIN)
                .address(Address.builder()
                        .country("Germany")
                        .region("Bavaria")
                        .city("Munich")
                        .street("Shlasse")
                        .number("23")
                        .postalCode("21-444")
                        .build())
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Test")
                .description("Test category description")
                .build());
        Discount discount = discountRepository.save(Discount.builder()
                .name("Test")
                .description("Test discount description")
                .discountPercent(25)
                .build());

        product1 = productRepository.save(Product.builder()
                .name("Water")
                .description("Water to drink")
                .amount(100)
                .price(2.5)
                .discount(discount)
                .categories(List.of(category))
                .build());

        product2 = productRepository.save(Product.builder()
                .name("Food")
                .description("Food to eat")
                .amount(150)
                .price(2.5)
                .discount(discount)
                .categories(List.of(category))
                .build());

        Order order1 = Order.builder()
                .orderDate(LocalDateTime.of(2017, 10,15,1,0))
                .totalPrice(100)
                .totalDiscount(10)
                .isCompleted(false)
                .user(user1)
                .address(user1.getAddress())
                .build();

        Order order2 = Order.builder()
                .orderDate(LocalDateTime.of(2019, 1,21,1,0))
                .totalPrice(100)
                .totalDiscount(10)
                .isCompleted(true)
                .user(user1)
                .address(user1.getAddress())
                .build();

        Order order3 = Order.builder()
                .orderDate(LocalDateTime.of(2020, 1,21,1,0))
                .totalPrice(100)
                .totalDiscount(10)
                .isCompleted(true)
                .user(user2)
                .address(user1.getAddress())
                .build();

        List<OrderProduct> orderProducts1 = new ArrayList<>();
        orderProducts1.add(orderProductRepository.save(OrderProduct.builder()
                .product(product1)
                .order(order1)
                .amount(5)
                .build()));

        List<OrderProduct> orderProducts2 = new ArrayList<>();
        orderProducts2.add(orderProductRepository.save(OrderProduct.builder()
                .product(product1)
                .order(order1)
                .amount(10)
                .build()));

        List<OrderProduct> orderProducts3 = new ArrayList<>();
        orderProducts3.add(orderProductRepository.save(OrderProduct.builder()
                .product(product2)
                .order(order3)
                .amount(5)
                .build()));

        order1.setOrderProducts(orderProducts1);
        order2.setOrderProducts(orderProducts2);
        order3.setOrderProducts(orderProducts3);

        testOrders.add(orderRepository.save(order1));

        testOrders.add(orderRepository.save(order2));

        testOrders.add(orderRepository.save(order3));
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    void contextLoads() throws Exception {
        assertThat(categoryRepository).isNotNull();
        assertThat(discountRepository).isNotNull();
        assertThat(productRepository).isNotNull();
        assertThat(userRepository).isNotNull();
        assertThat(orderRepository).isNotNull();
    }

    @Test
    void findAllByProductsProductIdFindsAllProperOrders() {
        testOrders.forEach(order -> orderRepository.save(order));

        List<Order> orders = orderRepository.findAll();
        assertEquals(orders.size(), testOrders.size());

        List<Order> foundWaterOrders = orderRepository.findAllByProductId(product1.getProductId());
        assertEquals(foundWaterOrders.size(), 2);
        foundWaterOrders.forEach(order -> order.getOrderProducts().forEach(
                orderProduct -> assertEquals(orderProduct.getProduct().getProductId(), product1.getProductId())
        ));

        List<Order> foundFoodOrders = orderRepository.findAllByProductId(product2.getProductId());
        assertEquals(foundFoodOrders.size(), 1);
        foundFoodOrders.forEach(order -> order.getOrderProducts().forEach(
                orderProduct -> assertEquals(orderProduct.getProduct().getProductId(), product2.getProductId())
        ));

        List<Order> notFoundOrders = orderRepository.findAllByProductId(999L);
        assertEquals(notFoundOrders.size(), 0);
    }

    @Test
    void findAllByOrderDateBetweenFindsAllProperOrders() {
        testOrders.forEach(order -> orderRepository.save(order));

        List<Order> orders = orderRepository.findAll();
        assertEquals(orders.size(), testOrders.size());

        List<Order> overlappedDateOrders = orderRepository.findAllByTimePeriod(
                LocalDateTime.of(2017, 10,15,1,0),
                LocalDateTime.of(2020, 1,21,1,0)
        );
        assertEquals(overlappedDateOrders.size(), 3);

        List<Order> fromDateBeforeToDateBeforeLastOrder = orderRepository.findAllByTimePeriod(
                LocalDateTime.of(2017, 10,15,1,0),
                LocalDateTime.of(2019, 1,21,1,0)
        );
        assertEquals(fromDateBeforeToDateBeforeLastOrder.size(), 2);

        List<Order> fromDateAfterToDateAfterFirstOrder = orderRepository.findAllByTimePeriod(
                LocalDateTime.of(2019, 1,21,1,0),
                LocalDateTime.of(2020, 1,21,1,0)
        );
        assertEquals(fromDateAfterToDateAfterFirstOrder.size(), 2);

        List<Order> sameDateFoundOrders = orderRepository.findAllByTimePeriod(
                LocalDateTime.of(2019, 1,21,1,0),
                LocalDateTime.of(2019, 1,21,1,0)
        );
        assertEquals(sameDateFoundOrders.size(), 1);

        List<Order> beforeEveryOrder = orderRepository.findAllByTimePeriod(
                LocalDateTime.of(2010, 1,21,1,0),
                LocalDateTime.of(2017, 10,15,0,59)
        );
        assertEquals(beforeEveryOrder.size(), 0);

        List<Order> afterEveryOrder = orderRepository.findAllByTimePeriod(
                LocalDateTime.of(2020, 1,21,1,1),
                LocalDateTime.of(2030, 10,15,0,59)
        );
        assertEquals(afterEveryOrder.size(), 0);

        List<Order> fromDateAfterBeforeDate = orderRepository.findAllByTimePeriod(
                LocalDateTime.of(2020, 1,21,1,0),
                LocalDateTime.of(2017, 10,15,1,0)
        );
        assertEquals(fromDateAfterBeforeDate.size(), 0);
    }

    @Test
    void findAllByUserUserIdFindsAllProperOrders() {
        testOrders.forEach(order -> orderRepository.save(order));

        List<Order> orders = orderRepository.findAll();
        assertEquals(orders.size(), testOrders.size());

        List<Order> firstUserOrders = orderRepository.findAllByUserId(user1.getUserId());
        assertEquals(firstUserOrders.size(), 2);
        firstUserOrders.forEach(order -> assertEquals(order.getUser().getUserId(), user1.getUserId()));

        List<Order> secondUserOrders = orderRepository.findAllByUserId(user2.getUserId());
        assertEquals(secondUserOrders.size(), 1);
        secondUserOrders.forEach(order -> assertEquals(order.getUser().getUserId(), user2.getUserId()));

        List<Order> nonExistentUserOrders = orderRepository.findAllByUserId(999L);
        assertEquals(nonExistentUserOrders.size(), 0);
    }

    @Test
    void findAllByIsCompletedFindsAllProperOrders() {
        testOrders.forEach(order -> orderRepository.save(order));

        List<Order> orders = orderRepository.findAll();
        assertEquals(orders.size(), testOrders.size());

        List<Order> completedOrders = orderRepository.findAllByCompletionStatus(true);
        assertEquals(completedOrders.size(), 2);
        completedOrders.forEach(order -> assertTrue(order.isCompleted()));

        List<Order> uncompletedOrders = orderRepository.findAllByCompletionStatus(false);
        assertEquals(uncompletedOrders.size(), 1);
        uncompletedOrders.forEach(order -> assertFalse(order.isCompleted()));
    }
}