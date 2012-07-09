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
public class Alert {

    private static Log logger = LogFactory.getLog(Alert.class);
    private String description;
    private Level type;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Level getType() {
        return type;
    }

    public void setType(Level level) {
        this.type = level;
    }

    public Alert(String description, Level level) {
        this.description = description;
        this.type = level;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("description", description);
            json.put("type", type.toString());
        } catch (JSONException jse) {
            logger.error("failed to create JSON for alert: " + jse.toString());
        }
        return json;
    }

    public static Alert fromJSON(JSONObject json) {
        try {
            String description = json.getString("description");
            Level type = Level.valueOf(json.getString("type"));
            return new Alert(description, type);
        } catch (JSONException jse) {
            throw new IllegalArgumentException("Couldn't create Alert from JSON", jse);
        }
    }
}
