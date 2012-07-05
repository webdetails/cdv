/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv.results;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cdv.notifications.Alert.Level;

/**
 *
 * @author pdpi
 */
public class ValidationResult {

    private static Log logger = LogFactory.getLog(ValidationResult.class);
    private Alert alert;
    private String description, name, type;

    public ValidationResult(Alert alert, String description, String name, String type) {
        this.alert = alert;
        this.description = description;
        this.name = name;
        this.type = type;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("name", name);
            json.put("description", description);
            json.put("alert", alert.toJSON());
        } catch (JSONException jse) {
            logger.error("Could't get JSON for Validation Result: " + jse.toString());
        }
        return json;
    }

    public static ValidationResult fromJSON(JSONObject json) {
        try {
            String name = json.getString("name");
            String description = json.getString("description");
            Alert alert = Alert.fromJSON(json.getJSONObject("alert"));
            String type = json.getString("type");
            return new ValidationResult(alert, description, name, type);
        } catch (JSONException jse) {
            throw new IllegalArgumentException("Error reading TestResult from JSON", jse);
        }
    }
}
