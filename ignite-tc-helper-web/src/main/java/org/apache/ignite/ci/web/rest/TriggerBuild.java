/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ci.web.rest;

import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.ignite.ci.ITeamcity;
import org.apache.ignite.ci.user.ICredentialsProv;
import org.apache.ignite.ci.web.CtxListener;
import org.apache.ignite.ci.web.rest.login.ServiceUnauthorizedException;
import org.apache.ignite.ci.web.model.SimpleResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Path("build")
@Produces(MediaType.APPLICATION_JSON)
public class TriggerBuild {
    @Context
    private ServletContext context;

    @Context
    private HttpServletRequest req;

    @GET
    @Path("trigger")
    public SimpleResult triggerBuild(
        @Nullable @QueryParam("serverId") String serverId,
        @Nullable @QueryParam("branchName") String branchName,
        @Nullable @QueryParam("suiteId") String suiteId,
        @Nullable @QueryParam("top") Boolean top) {

        final ICredentialsProv prov = ICredentialsProv.get(req);

        if(!prov.hasAccess(serverId)) {
            throw ServiceUnauthorizedException.noCreds(serverId);
        }

        try (final ITeamcity helper = CtxListener.getTcHelper(context).server(serverId, prov)) {
            helper.triggerBuild(suiteId, branchName, false, top != null && top);
        }

        return new SimpleResult("OK");
    }

    @GET
    @Path("triggerBuilds")
    public SimpleResult triggerBuilds(
        @Nullable @QueryParam("serverId") String serverId,
        @Nullable @QueryParam("branchName") String branchName,
        @NotNull @QueryParam("suiteIdList") String suiteIdList,
        @Nullable @QueryParam("top") Boolean top) {

        final ICredentialsProv prov = ICredentialsProv.get(req);

        if(!prov.hasAccess(serverId)) {
            throw ServiceUnauthorizedException.noCreds(serverId);
        }

        List<String> strings = Arrays.asList(suiteIdList.split(","));
        if (strings.isEmpty())
            return new SimpleResult("Error: nothing to run");

        try (final ITeamcity helper = CtxListener.getTcHelper(context).server(serverId, prov)) {
            boolean queueToTop = top != null && top;

            for (String suiteId : strings) {
                System.out.println("Triggering [ " + suiteId + "," + branchName + "," + "top=" + queueToTop + "]");

                helper.triggerBuild(suiteId, branchName, false, queueToTop);
            }
        }

        return new SimpleResult("OK");
    }

}
