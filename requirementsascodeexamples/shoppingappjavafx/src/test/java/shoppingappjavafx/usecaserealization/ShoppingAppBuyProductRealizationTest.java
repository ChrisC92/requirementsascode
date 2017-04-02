package shoppingappjavafx.usecaserealization;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.requirementsascode.TestUseCaseModelRunner;
import org.requirementsascode.UseCaseModel;
import org.requirementsascode.UseCaseModelBuilder;

import shoppingappjavafx.domain.PaymentDetails;
import shoppingappjavafx.domain.Product;
import shoppingappjavafx.domain.ShippingInformation;
import shoppingappjavafx.domain.Stock;
import shoppingappjavafx.usecase.ShoppingAppModelBuilder;
import shoppingappjavafx.usecase.event.AddProductToCart;
import shoppingappjavafx.usecase.event.CheckoutPurchase;
import shoppingappjavafx.usecase.event.ConfirmPurchase;
import shoppingappjavafx.usecase.event.EnterPaymentDetails;
import shoppingappjavafx.usecase.event.EnterShippingInformation;
import shoppingappjavafx.usecaserealization.stubs.DisplayStub;

public class ShoppingAppBuyProductRealizationTest {
	private TestUseCaseModelRunner useCaseModelRunner;
	private UseCaseModel useCaseModel;

	@Before
	public void setUp() throws Exception {
		useCaseModelRunner = new TestUseCaseModelRunner();
		UseCaseModelBuilder modelBuilder = UseCaseModelBuilder.newBuilder();
		
		Stock stock = new Stock();
		Display displayStub = new DisplayStub();
		
		ShoppingAppBuyProductRealization shoppingAppUseCaseRealization =
			new ShoppingAppBuyProductRealization(stock, displayStub);
		useCaseModel = 
			new ShoppingAppModelBuilder(shoppingAppUseCaseRealization).buildWith(modelBuilder);
	}

	@Test
	public void runsBasicFlow() {
		useCaseModelRunner.run(useCaseModel);
		useCaseModelRunner.reactTo(
			new AddProductToCart(new Product("Hamster Wheel, Black", new BigDecimal(9.95))),
			new CheckoutPurchase(),
			new EnterShippingInformation(new ShippingInformation()),
			new EnterPaymentDetails(new PaymentDetails()),
			new ConfirmPurchase());
		
		assertEquals("S1;S2;S3;S4;S5;S6;S7;S8;S9;S10;S11;S1;S2;", useCaseModelRunner.getRunStepNames());
	}
}
