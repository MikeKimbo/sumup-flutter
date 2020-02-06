package florent.sumup_flutter;

import android.app.Activity;
import android.content.Intent;

import com.sumup.merchant.api.SumUpAPI;
import com.sumup.merchant.api.SumUpLogin;
import com.sumup.merchant.api.SumUpPayment;
import com.sumup.merchant.api.SumUpState;
import com.sumup.merchant.ui.Activities.SumUpBaseActivity;

import java.math.BigDecimal;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** SumupFlutterPlugin */
public class SumupFlutterPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {

  private final Activity activity;
  static String APIKey;
  Result result;
  private static final int REQUEST_CODE_PAYMENT = 2;

  private SumupFlutterPlugin(Activity activity) {
    this.activity = activity;
    SumUpState.init(this.activity);
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "sumup_flutter");
    final SumupFlutterPlugin instance = new SumupFlutterPlugin(registrar.activity());
    registrar.addActivityResultListener(instance);
    channel.setMethodCallHandler(instance);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_PAYMENT && data != null) {
      // A payment has completed. Put together the transaction info as a JSON string.
      String info = "{\r\n" +
      "\t\"resultcode\":" + data.getExtras().getInt(SumUpAPI.Response.RESULT_CODE) + ",\r\n" +
      "\t\"message\":\"" + data.getExtras().getString(SumUpAPI.Response.MESSAGE) + "\",\r\n" +
      "\t\"txcode\":\"" + data.getExtras().getString(SumUpAPI.Response.TX_CODE) + "\"\r\n" +
      "}";

      // Return it to the client.
      this.result.success(info);
    }
    return false;
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("authenticate")) {
      String APIKey = call.argument("APIKey").toString();
      authenticate(APIKey);
      result.success(null);
    } else if (call.method.equals("presentLoginView")) {
      login();
      result.success(true);
    } else if (call.method.equals("sendCheckout")) {
      String amount = call.argument("amount").toString();
      String title = call.argument("title").toString();
      String currencyCode = call.argument("currencyCode").toString();
      this.result = result;
      checkout(amount, title, currencyCode);
    } else if (call.method.equals("isLoggedIn")) {
      result.success(isLoggedIn());
    } else if (call.method.equals("logOut")) {
      logOut();
      result.success(true);
    }
    else {
      result.notImplemented();
    }
  }

  boolean isLoggedIn() {
    return SumUpAPI.isLoggedIn();
  }

  void authenticate(String APIKey) {
    SumupFlutterPlugin.APIKey = APIKey;
  }

  void login() {
    SumUpLogin sumupLogin = SumUpLogin.builder(SumupFlutterPlugin.APIKey).build();
    SumUpAPI.openLoginActivity(activity, sumupLogin, 1);
  }

  void logOut() {
    SumUpAPI.logout();
  }

  void checkout(String amount, String title, String currencyCode) {
    SumUpPayment payment = SumUpPayment.builder()
            .total(new BigDecimal(amount))
            .currency(SumUpPayment.Currency.valueOf(currencyCode))
            .title(title)
            .build();

    SumUpAPI.checkout(activity, payment, REQUEST_CODE_PAYMENT);
  }



}
