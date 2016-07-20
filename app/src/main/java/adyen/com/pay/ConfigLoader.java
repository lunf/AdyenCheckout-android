package adyen.com.pay;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import adyen.com.adyenpaysdk.controllers.NetworkController;


/**
 * Created by andrei on 11/10/15.
 */
public class ConfigLoader {

    private String tag = ConfigLoader.class.getSimpleName();
    private Context mContext;

    public ConfigLoader(Context context) {
        mContext = context;
    }

    public JSONObject loadJsonConfiguration() {
        JSONObject configurationJson;
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.config);

        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }

            configurationJson = new JSONObject(responseStrBuilder.toString());

            return configurationJson;
        } catch (IOException e) {
            Log.e(tag, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(tag, e.getMessage(), e);
        }

        return null;
    }

}
