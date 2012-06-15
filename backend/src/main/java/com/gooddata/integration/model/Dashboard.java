package com.gooddata.integration.model;

/**
 * GoodData dashboard object wrapper
 *
 * @author Khaled Ayoubi
 */

import net.sf.json.JSONObject;

public class Dashboard {
	private String uri;
    private String id;
    private String title;

    /**
     * Constructs the GoodData dashboard from the JSON structure
     *
     * @param obj the JSON structure taken from the GoodData REST API
     */
    public Dashboard(JSONObject obj) {
        super();
        uri = obj.getString("uri");
        id = obj.getString("identifier");
        title = obj.getString("title");
    }

    /**
     * Returns the dashboard URI
     *
     * @return the dashboard URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns the dashboard ID
     *
     * @return the dashboard ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the dashboard name
     *
     * @return the dashboard name
     */
    public String getName() {
        return title;
    }
}
