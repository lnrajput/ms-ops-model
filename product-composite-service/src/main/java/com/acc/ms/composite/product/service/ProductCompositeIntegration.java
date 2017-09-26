package com.acc.ms.composite.product.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import com.acc.ms.model.Product;
import com.acc.ms.model.Recommendation;
import com.acc.ms.model.Review;
import com.acc.ms.util.ServiceUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Component
public class ProductCompositeIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final ServiceUtils util;
    private final RestOperations restTemplate;

    @Inject
    public ProductCompositeIntegration(ServiceUtils util, RestOperations restTemplate) {
        this.util = util;
        this.restTemplate = restTemplate;
    }

    // -------- //
    // PRODUCTS //
    // -------- //

    @HystrixCommand(fallbackMethod = "defaultProduct")
    public ResponseEntity<Product> getProduct(int productId) {

        LOG.debug("Will call getProduct with Hystrix protection");

        String url = "http://product-service/product/" + productId;
        LOG.debug("GetProduct from URL: {}", url);

        ResponseEntity<String> resultStr = restTemplate.getForEntity(url, String.class);
        LOG.debug("GetProduct http-status: {}", resultStr.getStatusCode());
        LOG.debug("GetProduct body: {}", resultStr.getBody());

        Product product = response2Product(resultStr);
        LOG.debug("GetProduct.id: {}", product.getProductId());

        return util.createOkResponse(product);
    }

    /**
     * Fallback method for getProduct()
     *
     * @param productId
     * @return
     */
    public ResponseEntity<Product> defaultProduct(int productId) {
        LOG.warn("Using fallback method for product-service with productId: {}", productId);
        return util.createResponse(
            new Product(productId, "Fallback Name", -1, ""),
            HttpStatus.OK);
    }

    // --------------- //
    // RECOMMENDATIONS //
    // --------------- //

    @HystrixCommand(fallbackMethod = "defaultRecommendations")
    public ResponseEntity<List<Recommendation>> getRecommendations(int productId) {
        try {
            LOG.debug("Will call getRecommendations with Hystrix protection");

            String url = "http://recommendation-service/recommendation?productId=" + productId;
            LOG.debug("GetRecommendations from URL: {}", url);

            ResponseEntity<String> resultStr = restTemplate.getForEntity(url, String.class);
            LOG.debug("GetRecommendations http-status: {}", resultStr.getStatusCode());
            LOG.debug("GetRecommendations body: {}", resultStr.getBody());

            List<Recommendation> recommendations = response2Recommendations(resultStr);
            LOG.debug("GetRecommendations.cnt {}", recommendations.size());

            return util.createOkResponse(recommendations);
        } catch (Throwable t) {
            LOG.error("getRecommendations error", t);
            throw t;
        }
    }


    /**
     * Fallback method for getRecommendations()
     *
     * @param productId
     * @return
     */
    public ResponseEntity<List<Recommendation>> defaultRecommendations(int productId) {
        LOG.warn("Using fallback method for recommendation-service with productId: {}", productId);
        return util.createResponse(
            Arrays.asList(new Recommendation(productId, 1, "Fallback Author 1", 1, "Fallback Content 1", "")),
            HttpStatus.OK);
    }


    // ------- //
    // REVIEWS //
    // ------- //

    @HystrixCommand(fallbackMethod = "defaultReviews")
    public ResponseEntity<List<Review>> getReviews(int productId) {
        LOG.debug("Will call getReviews with Hystrix protection");

        String url = "http://review-service/review?productId=" + productId;
        LOG.debug("GetReviews from URL: {}", url);

        ResponseEntity<String> resultStr = restTemplate.getForEntity(url, String.class);
        LOG.debug("GetReviews http-status: {}", resultStr.getStatusCode());
        LOG.debug("GetReviews body: {}", resultStr.getBody());

        List<Review> reviews = response2Reviews(resultStr);
        LOG.debug("GetReviews.cnt {}", reviews.size());

        return util.createOkResponse(reviews);
    }


    /**
     * Fallback method for getReviews()
     *
     * @param productId
     * @return
     */
    public ResponseEntity<List<Review>> defaultReviews(int productId) {
        LOG.warn("Using fallback method for review-service with productId: {}", productId);
        return util.createResponse(
            Arrays.asList(new Review(productId, 1, "Fallback Author 1", "Fallback Subject 1", "Fallback Content 1", "")),
            HttpStatus.OK);
    }

    // ----- //
    // UTILS //
    // ----- //

    /*
     * TODO: Extract to a common util-lib
     */

    private ObjectReader productReader = null;
    private ObjectReader getProductReader() {

        if (productReader != null) return productReader;

        ObjectMapper mapper = new ObjectMapper();
        return productReader = mapper.reader(Product.class);
    }

    private ObjectReader reviewsReader = null;
    private ObjectReader getReviewsReader() {
        if (reviewsReader != null) return reviewsReader;

        ObjectMapper mapper = new ObjectMapper();
        return reviewsReader = mapper.reader(new TypeReference<List<Review>>() {});
    }

    public Product response2Product(ResponseEntity<String> response) {
        try {
            return getProductReader().readValue(response.getBody());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Gereralize with <T> method, skip objectReader objects!
    private List<Recommendation> response2Recommendations(ResponseEntity<String> response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List list = mapper.readValue(response.getBody(), new TypeReference<List<Recommendation>>() {});
            List<Recommendation> recommendations = list;
            return recommendations;

        } catch (IOException e) {
            LOG.warn("IO-err. Failed to read JSON", e);
            throw new RuntimeException(e);

        } catch (RuntimeException re) {
            LOG.warn("RTE-err. Failed to read JSON", re);
            throw re;
        }
    }

    private List<Review> response2Reviews(ResponseEntity<String> response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List list = mapper.readValue(response.getBody(), new TypeReference<List<Review>>() {});
            List<Review> reviews = list;
            return reviews;

        } catch (IOException e) {
            LOG.warn("IO-err. Failed to read JSON", e);
            throw new RuntimeException(e);

        } catch (RuntimeException re) {
            LOG.warn("RTE-err. Failed to read JSON", re);
            throw re;
        }
    }

// FIXME: DOESN'T WORK. GIVER ERORS LIKE: Caused by: java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to se.callista.microservises.core.recommendation.model.Recommendation
//    private <T> T responseString2Type(ResponseEntity<String> response) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            T object = mapper.readValue(response.getBody(), new TypeReference<T>() {});
//            return object;
//
//        } catch (IOException e) {
//            LOG.warn("IO-err. Failed to read JSON", e);
//            throw new RuntimeException(e);
//
//        } catch (RuntimeException re) {
//            LOG.warn("RTE-err. Failed to read JSON", re);
//            throw re;
//        }
//    }
//
//    /**
//     * TODO: DO WE REALLY NEED THIS ONE???
//     *
//     * @param response
//     * @param <T>
//     * @return
//     */
//    private <T> List<T> responseString2List(ResponseEntity<String> response) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            List<T> list = mapper.readValue(response.getBody(), new TypeReference<List<T>>() {});
//            return list;
//
//        } catch (IOException e) {
//            LOG.warn("IO-err. Failed to read JSON", e);
//            throw new RuntimeException(e);
//
//        } catch (RuntimeException re) {
//            LOG.warn("RTE-err. Failed to read JSON", re);
//            throw re;
//        }
//    }
//

}