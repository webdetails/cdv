/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv.results;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cdv.notifications.Alert.Level;

/**
 *
 * @author pdpi
 */
public class TestResult {

    private static Log logger = LogFactory.getLog(TestResult.class);
    private List<ValidationResult> results;
    private Date timestamp;
    private Level overallLevel, durationLevel;
    private String overallDescription, name, group;
    Alert cause;
    private int expectedDuration, actualDuration;

    public TestResult(String json) {
        try{
        init(new JSONObject(json));
        } catch(JSONException jse) {
            throw new IllegalArgumentException("Couldn't restore TestResult from JSON", jse);
        }
    }

    public TestResult(JSONObject json) {
        init(json);
    }

    private void init(JSONObject json) {
        try {
            timestamp = new Date(json.getLong("timestamp"));

            JSONObject duration = json.getJSONObject("duration");
            expectedDuration = duration.getInt("expected");
            actualDuration = duration.getInt("duration");
            durationLevel = Level.valueOf(duration.getString("type"));

            JSONObject test = json.getJSONObject("test");
            name = test.getString("name");
            group = test.getString("group");

            JSONObject testResult = json.getJSONObject("testResult");
            cause = Alert.fromJSON(testResult.getJSONObject("cause"));
            overallDescription = testResult.getString("description");
            overallLevel = Level.valueOf(testResult.getString("type"));

            results = new ArrayList<ValidationResult>();
            JSONArray validationResults = json.getJSONArray("validationResults");
            for (int i = 0; i < validationResults.length(); i++) {
                JSONObject validation = validationResults.getJSONObject(i);
                results.add(ValidationResult.fromJSON(validation));
            }

        } catch (JSONException jse) {
            throw new IllegalArgumentException("Error reading TestResult from JSON", jse);
        }
    }

    
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        try {
            JSONObject testResult = new JSONObject();
            testResult.put("cause", cause.toJSON());
            testResult.put("type", overallLevel.toString());
            testResult.put("description", overallDescription);
            json.put("testResult", testResult);

            JSONArray validationResults = new JSONArray();
            for (ValidationResult res : results) {
                validationResults.put(res.toJSON());
            }
            json.put("validationResults", validationResults);

            JSONObject durationResult = new JSONObject();
            durationResult.put("type", durationLevel.toString());
            durationResult.put("expected", expectedDuration);
            durationResult.put("duration", actualDuration);
            json.put("duration", durationResult);

            json.put("timestamp", timestamp.getTime());

            JSONObject test = new JSONObject();
            test.put("name", name);
            test.put("group", group);
            json.put("test", test);
        } catch (JSONException jse) {
            logger.error("Could't get JSON for Validation Result: " + jse.toString());
        }
        return json;
    }

    public static TestResult fromJSON(JSONObject json) {
        TestResult result = new TestResult(json);
        return result;
    }
}
