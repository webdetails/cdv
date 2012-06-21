/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv.notifications;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cpf.persistence.Persistable;

/**
 *
 * @author pdpi
 */
public class Alert implements Persistable {

    public enum Level {

        OK, WARNING, ERROR, CRITICAL
    }
    private Level level;
    private String msg, group;
    private Date timestamp;

    public Alert(Level level, String group, String msg) {
        init(level, group, msg, new Date());
    }

    public Alert(String level, String group, String msg) {
        init(Level.valueOf(level.toUpperCase()), group, msg, new Date());
    }

    private Alert(JSONObject json) {
        try {
            Level lvl = Level.valueOf(json.getString("level").toUpperCase());
            String g = json.getString("group"),
                    m = json.getString("message");
            init(lvl, g, m, new Date());
        } catch (JSONException e) {
        }
    }

    public static Alert fromJSON(JSONObject json) {
        Alert alert = new Alert(json);
        try {
            alert.timestamp = new Date(json.getLong("timestamp"));
        } catch (JSONException jse) {
            // Do nothing
        }
        return alert;
    }

    private void init(Level level, String group, String msg, Date date) {
        this.level = level;
        this.msg = msg;
        this.group = group;
        this.timestamp = date;
    }

    public Level getLevel() {
        return level;
    }

    public String getGroup() {
        return group;
    }

    public String getMessage() {
        return msg;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getPersistenceClass() {
        return "Alert";
    }

    public JSONObject getKey() {
        JSONObject json = new JSONObject();
        try {
            json.put("level", level.toString());
            json.put("group", group);
            json.put("timestamp", timestamp.getTime());
        } catch (JSONException jse) {
        }
        return json;
    }

    public JSONObject toJSON() {
        JSONObject json = getKey();
        try {
            json.put("message", msg);
        } catch (JSONException jse) {
        }
        return json;
    }
}
