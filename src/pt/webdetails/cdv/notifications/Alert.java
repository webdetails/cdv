package pt.webdetails.cdv.notifications;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cpf.persistence.Persistable;

/**
 *
 * @author pdpi
 */
public class Alert {

    public enum Level {

        ALL, OK, WARN, ERROR, CRITICAL
    }
    private Level level;
    private String msg, group, name, summary;
    private Date timestamp;

    public Alert(Level level, String group, String name, String msg, String summary) {
        init(level, group, name, msg, summary, new Date());
    }

 //   public Alert(String level, String group, String name, String msg) {
 //       init(Level.valueOf(level.toUpperCase()), group, name, msg, null, new Date());
 //   }

    private Alert(JSONObject json) {
        try {
            Level lvl = Level.valueOf(json.getString("level").toUpperCase());
            String g = json.getString("group"),
                    n = json.getString("group"),
                    m = json.getString("message"),
                    s = json.getString("summary");
            init(lvl, g, n, m, s, new Date());
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

    private void init(Level level, String group, String name, String msg, String summary, Date date) {
        this.level = level;
        this.msg = msg;
        this.name = name;
        this.group = group;
        this.timestamp = date;
        this.summary = summary;
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

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }
            
    public String getPersistenceClass() {
        return "Alert";
    }

    public JSONObject getKey() {
        JSONObject json = new JSONObject();
        try {
            json.put("level", level.toString());
            json.put("group", group);
            json.put("name", name);
            json.put("timestamp", timestamp.getTime());
        } catch (JSONException jse) {
        }
        return json;
    }

    public JSONObject toJSON() {
        JSONObject json = getKey();
        try {
            json.put("summary", summary);
            json.put("message", msg);
        } catch (JSONException jse) {
        }
        return json;
    }
}
