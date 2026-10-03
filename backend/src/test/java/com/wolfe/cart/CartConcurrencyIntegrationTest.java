package com.wolfe.cart;

import static org.junit.jupiter.api.Assertions.assertThrows;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@Tag("integration")
@SpringBootTest
class CartConcurrencyIntegrationTest {
  @Test void databaseUniqueExpressionPreventsDuplicateCartLine(@Autowired DataSource dataSource) {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    long customer = jdbc.queryForObject("insert into customers(name,email,password_hash) values ('cart-test','cart-concurrency-'||extract(epoch from clock_timestamp())||'@example.test','x') returning id", Long.class);
    long product = jdbc.queryForObject("insert into products(slug,name,price,category,finish,description) values ('cart-test-'||extract(epoch from clock_timestamp()),'Cart Test',10,'Test','Metal','test') returning id", Long.class);
    try {
      jdbc.update("insert into cart_items(customer_id,product_id,quantity) values (?,?,1)", customer, product);
      assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("insert into cart_items(customer_id,product_id,quantity) values (?,?,1)", customer, product));
    } finally {
      jdbc.update("delete from cart_items where customer_id=?", customer);
      jdbc.update("delete from products where id=?", product);
      jdbc.update("delete from customers where id=?", customer);
    }
  }
}
