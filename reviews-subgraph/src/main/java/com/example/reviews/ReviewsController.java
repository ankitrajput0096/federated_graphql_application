package com.example.reviews;

import java.util.List;

import com.example.reviews.model.Product;
import com.example.reviews.model.Review;
import com.example.reviews.model.ReviewSource;

import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ReviewsController {

  @EntityMapping
  public Product product(@Argument Long id) {
    return new Product(id);
  }

  @SchemaMapping
  public List<Review> reviews(Product product) {
    return ReviewSource.getReviews(product);
  }

  @QueryMapping
  public List<Review> reviews(@Argument Long productId) {
    return ReviewSource.getReviews(new Product(productId));
  }

  @QueryMapping
  public List<Review> allReviews() {
      List<Review> reviews = ReviewSource.getAllReviews();
      return reviews;
  }
}
