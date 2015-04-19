package cern.modesti.repository.request.schema;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author Justin Lewis Salmon
 */
@Controller
@RequestMapping("/schemas")
@ExposesResourceFor(Schema.class)
public class SchemaController {
  
  @Autowired
  SchemaRepository schemaRepository;
  
  @RequestMapping(value = "/{id}", method = GET)
  HttpEntity<Schema> getSchema(@PathVariable("id") String id) {
    return new HttpEntity<Schema>(schemaRepository.findOne(id));
  }
  

//  private final @NonNull PaymentService paymentService;
//  private final @NonNull EntityLinks entityLinks;

//  /**
//   * Accepts a payment for an {@link Order}
//   * 
//   * @param order the {@link Order} to process the payment for. Retrieved from the path variable and converted into an
//   *          {@link Order} instance by Spring Data's {@link DomainClassConverter}. Will be {@literal null} in case no
//   *          {@link Order} with the given id could be found.
//   * @param number the {@link CreditCardNumber} unmarshalled from the request payload.
//   * @return
//   */
//  @RequestMapping(/*value = PaymentLinks.PAYMENT, */method = PUT)
//  ResponseEntity<PaymentResource> submitPayment(/*@PathVariable("id") Order order, @RequestBody CreditCardNumber number*/) {

//    if (order == null || order.isPaid()) {
//      return new ResponseEntity<PaymentResource>(HttpStatus.NOT_FOUND);
//    }
//
//    CreditCardPayment payment = paymentService.pay(order, number);
//
//    PaymentResource resource = new PaymentResource(order.getPrice(), payment.getCreditCard());
//    resource.add(entityLinks.linkToSingleResource(order));
//
//    return new ResponseEntity<PaymentResource>(resource, HttpStatus.CREATED);
//  }

//  /**
//   * Shows the {@link Receipt} for the given order.
//   * 
//   * @param order
//   * @return
//   */
//  @RequestMapping(/*value = PaymentLinks.RECEIPT,*/ method = GET)
//  HttpEntity<Resource<Receipt>> showReceipt(@PathVariable("id") Order order) {
//
//    if (order == null || !order.isPaid() || order.isTaken()) {
//      return new ResponseEntity<Resource<Receipt>>(HttpStatus.NOT_FOUND);
//    }
//
//    return paymentService.getPaymentFor(order).//
//        map(payment -> createReceiptResponse(payment.getReceipt())).//
//        orElseGet(() -> new ResponseEntity<Resource<Receipt>>(HttpStatus.NOT_FOUND));
//  }

//  /**
//   * Takes the {@link Receipt} for the given {@link Order} and thus completes the process.
//   * 
//   * @param order
//   * @return
//   */
//  @RequestMapping(value = PaymentLinks.RECEIPT, method = DELETE)
//  HttpEntity<Resource<Receipt>> takeReceipt(@PathVariable("id") Order order) {
//
//    if (order == null || !order.isPaid()) {
//      return new ResponseEntity<Resource<Receipt>>(HttpStatus.NOT_FOUND);
//    }
//
//    return paymentService.takeReceiptFor(order).//
//        map(receipt -> createReceiptResponse(receipt)).//
//        orElseGet(() -> new ResponseEntity<Resource<Receipt>>(HttpStatus.METHOD_NOT_ALLOWED));
//  }

//  /**
//   * Renders the given {@link Receipt} including links to the associated {@link Order} as well as a self link in case
//   * the {@link Receipt} is still available.
//   * 
//   * @param receipt
//   * @return
//   */
//  private HttpEntity<Resource<Receipt>> createReceiptResponse(Receipt receipt) {
//
//    Order order = receipt.getOrder();
//
//    Resource<Receipt> resource = new Resource<Receipt>(receipt);
//    resource.add(entityLinks.linkToSingleResource(order));
//
//    if (!order.isTaken()) {
//      resource.add(entityLinks.linkForSingleResource(order).slash("receipt").withSelfRel());
//    }
//
//    return new ResponseEntity<Resource<Receipt>>(resource, HttpStatus.OK);
//  }
//
//  /**
//   * Resource implementation for payment results.
//   * 
//   * @author Oliver Gierke
//   */
//  @Data
//  @EqualsAndHashCode(callSuper = true)
//  static class PaymentResource extends ResourceSupport {
//
//    private final MonetaryAmount amount;
//    private final CreditCard creditCard;
//  }

}