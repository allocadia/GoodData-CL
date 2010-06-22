/*
 * Copyright (c) 2009, GoodData Corporation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
 *        the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
 *        or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.gooddata.processor;

import com.gooddata.exception.GdcLoginException;
import com.gooddata.exception.InvalidParameterException;
import com.gooddata.integration.ftp.GdcFTPApiWrapper;
import com.gooddata.integration.rest.GdcRESTApiWrapper;
import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
import org.apache.log4j.Logger;
import org.gooddata.connector.Connector;

/**
 * GoodData
 *
 * @author zd <zd@gooddata.com>
 * @version 1.0
 */
public class ProcessingContext {

    private static Logger l = Logger.getLogger(ProcessingContext.class);

    private String projectId;
    private Connector connector;
    private GdcRESTApiWrapper _restApi = null;
    private GdcFTPApiWrapper _ftpApi = null;


    public String getProjectId() throws InvalidParameterException {
        if(projectId == null || projectId.length() <= 0)
            throw new InvalidParameterException("No project is active. Please activate project via CreateProject or " +
                    "OpenProject command. ");
        else
            return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public GdcRESTApiWrapper getRestApi(CliParams cliParams) throws GdcLoginException {
    	if (_restApi == null) {
            NamePasswordConfiguration httpConfig = cliParams.getHttpConfig();
            l.debug("Using the GoodData HTTP host '" + httpConfig.getGdcHost() + "'.");
            _restApi = new GdcRESTApiWrapper(httpConfig);
            _restApi.login();
    	}
    	return _restApi;
    }

    public GdcFTPApiWrapper getFtpApi(CliParams cliParams) {
    	if (_ftpApi == null) {
            NamePasswordConfiguration ftpConfig = cliParams.getFtpConfig();
	        l.debug("Using the GoodData FTP host '" + ftpConfig.getGdcHost() + "'.");
	        _ftpApi = new GdcFTPApiWrapper(ftpConfig);
    	}
    	return _ftpApi;
    }

}
